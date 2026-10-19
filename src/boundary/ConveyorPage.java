package boundary;

import control.ConveyorControl;
import entity.Conveyor;

import javax.swing.*;
import java.awt.*;

public class ConveyorPage extends JPanel {

    private final ConveyorControl conveyorControl = new ConveyorControl();

    private final JTextField idTf = new JTextField(10);
    private final JLabel statusLbl = MainFrame.subtitle("Status: —");
    private final JTextArea log = new JTextArea(12, 60);

    public ConveyorPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        // Header
        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Conveyor Control"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Manage conveyor integrity checks and daily operations"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));

        // Top row: ID + Refresh + status
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topRow.setOpaque(false);

        JLabel idLbl = new JLabel("Conveyor ID:");
        idLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        idTf.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton refresh = MainFrame.secondaryButton("Refresh Status");
        refresh.addActionListener(e -> refreshStatus());

        topRow.add(idLbl);
        topRow.add(idTf);
        topRow.add(refresh);
        topRow.add(Box.createHorizontalStrut(10));
        topRow.add(statusLbl);

        // Daily Operations (ALL) - big visible buttons
        JPanel dailyOps = new JPanel(new GridLayout(1, 2, 10, 10));
        dailyOps.setOpaque(false);
        dailyOps.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton onAll = MainFrame.primaryButton("Turn On ALL Conveyors");
        JButton offAll = MainFrame.secondaryButton("Turn Off ALL Conveyors");

        onAll.addActionListener(e -> doActionNoId("Turn On ALL", () -> conveyorControl.turnOnConveyors()));
        offAll.addActionListener(e -> doActionNoId("Turn Off ALL", () -> conveyorControl.turnOffConveyors()));

        dailyOps.add(onAll);
        dailyOps.add(offAll);

        // Single conveyor actions
        JPanel actions = new JPanel(new GridLayout(1, 5, 10, 10));
        actions.setOpaque(false);
        actions.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton on = MainFrame.primaryButton("Turn On");
        JButton off = MainFrame.secondaryButton("Turn Off");
        JButton pause = MainFrame.secondaryButton("Pause");
        JButton resume = MainFrame.secondaryButton("Resume");
        JButton restart = MainFrame.secondaryButton("Restart");

        on.addActionListener(e -> doAction("Turn On", () -> conveyorControl.turnOn(readId())));
        off.addActionListener(e -> doAction("Turn Off", () -> conveyorControl.turnOff(readId())));
        pause.addActionListener(e -> doAction("Pause", () -> conveyorControl.pause(readId())));
        resume.addActionListener(e -> doAction("Resume", () -> conveyorControl.resume(readId())));
        restart.addActionListener(e -> doAction("Restart", () -> conveyorControl.restart(readId())));

        actions.add(on);
        actions.add(off);
        actions.add(pause);
        actions.add(resume);
        actions.add(restart);

        // Logs
        log.setFont(new Font("Consolas", Font.PLAIN, 12));
        log.setEditable(false);
        log.setLineWrap(true);
        log.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(log);

        JPanel centerStack = new JPanel();
        centerStack.setOpaque(false);
        centerStack.setLayout(new BoxLayout(centerStack, BoxLayout.Y_AXIS));
        centerStack.add(topRow);
        centerStack.add(dailyOps);
        centerStack.add(actions);
        centerStack.add(sp);

        body.add(centerStack, BorderLayout.CENTER);
        root.add(body, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        JButton back = MainFrame.secondaryButton("Back to Dashboard");
        back.addActionListener(e -> frame.showPage(MainFrame.PAGE_DASHBOARD));
        footer.add(back);

        root.add(footer, BorderLayout.SOUTH);

        add(root);

        appendLog("Ready. Enter Conveyor ID then Refresh Status, or use Turn On ALL for daily start.");
    }

    private int readId() {
        String s = idTf.getText().trim();
        if (!s.matches("\\d+")) throw new IllegalArgumentException("Conveyor ID must be a number.");
        return Integer.parseInt(s);
    }

    private void refreshStatus() {
        int id;
        try {
            id = readId();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        appendLog("Refreshing status for ID=" + id + "...");

        SwingWorker<Conveyor, Void> worker = new SwingWorker<>() {
            @Override
            protected Conveyor doInBackground() throws Exception {
                return conveyorControl.getById(id);
            }

            @Override
            protected void done() {
                try {
                    Conveyor c = get();
                    if (c == null) {
                        statusLbl.setText("Status: Not found");
                        appendLog("No conveyor found with ID=" + id);
                        return;
                    }

                    String reason = c.getLastFailureReason();
                    if (reason == null || reason.isBlank()) reason = "—";

                    statusLbl.setText(
                            "Status: " + c.getState()
                                    + " | Busy: " + c.isBusy()
                                    + " | Attempts: " + c.getAttemptCount()
                                    + " | LastFailure: " + reason
                    );

                    appendLog("Loaded: ID=" + c.getConveyorID()
                            + " lot=" + c.getLotNumber()
                            + " maxKg=" + c.getMaxWeightKg()
                            + " pos=(" + c.getFloorNumber() + "," + c.getPositionX() + "," + c.getPositionY() + ")"
                            + " pendingWeight=" + c.isPendingWeight()
                            + " confirmed=" + c.isWeightConfirmed());

                } catch (Exception ex) {
                    statusLbl.setText("Status: Error");
                    appendLog("ERROR: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ConveyorPage.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void doAction(String actionName, ControlAction action) {
        int id;
        try {
            id = readId();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid ID", JOptionPane.ERROR_MESSAGE);
            return;
        }

        appendLog(actionName + " requested for ID=" + id + "...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return action.run();
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    appendLog("OK:\n" + msg);
                    refreshStatus();
                } catch (Exception ex) {
                    appendLog("ERROR: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ConveyorPage.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void doActionNoId(String actionName, ControlAction action) {
        appendLog(actionName + " requested...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                return action.run();
            }

            @Override
            protected void done() {
                try {
                    String msg = get();
                    appendLog("OK:\n" + msg);
                } catch (Exception ex) {
                    appendLog("ERROR: " + ex.getMessage());
                    JOptionPane.showMessageDialog(ConveyorPage.this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void appendLog(String s) {
        log.append("• " + s + "\n");
        log.setCaretPosition(log.getDocument().getLength());
    }

    @FunctionalInterface
    private interface ControlAction {
        String run() throws Exception;
    }
}
