package boundary;

import control.ConveyorAssignmentResult;
import control.ConveyorControllerControl;

import javax.swing.*;
import java.awt.*;

public class ConveyorControllerPage extends JPanel {

    private final MainFrame frame;
    private final ConveyorControllerControl control = new ConveyorControllerControl();

    private final JTextField vehicleNumberField = new JTextField(14);
    private final JTextArea output = new JTextArea(10, 50);

    public ConveyorControllerPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Conveyor Controller"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Scan a vehicle number to assign conveyor + nearest compatible space"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());

        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Vehicle Number:"), gc);

        gc.gridx = 1;
        form.add(vehicleNumberField, gc);

        JButton parkBtn = MainFrame.primaryButton("Park Vehicle");
        parkBtn.addActionListener(e -> onPark());

        JButton backBtn = MainFrame.secondaryButton("Back to Dashboard");
        backBtn.addActionListener(e -> frame.showDashboard());

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2;
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(parkBtn);
        btnRow.add(Box.createRigidArea(new Dimension(12, 0)));
        btnRow.add(backBtn);
        form.add(btnRow, gc);

        root.add(form, BorderLayout.CENTER);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(output);
        scroll.setBorder(BorderFactory.createTitledBorder("Output"));
        root.add(scroll, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void onPark() {
        output.setText("");
        String vehicle = vehicleNumberField.getText().trim();

        ConveyorAssignmentResult r = control.parkVehicle(vehicle);

        output.append(r.getMessage() + "\n");
        if (r.isSuccess()) {
            output.append("SessionID: " + r.getSessionId() + "\n");
            output.append("ConveyorID: " + r.getChosenConveyorId() + "\n");
            output.append("SpaceID: " + r.getChosenSpaceId() + "\n");
        }
    }
}
