package boundary;

import control.ParkingSessionViewControl;
import control.ParkingSessionViewRow;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ParkingSessionPage extends JPanel {

    private final MainFrame frame;
    private final ParkingSessionViewControl control = new ParkingSessionViewControl();

    private final JTextField vehicleField = new JTextField(10);
    private final JTextField phoneField = new JTextField(12);
    private final JTextField lotField = new JTextField(6);
    private final JCheckBox activeOnly = new JCheckBox("Active only", true);

    private final JLabel status = MainFrame.subtitle(" ");

    private final DefaultTableModel model;
    private final JTable table;

    public ParkingSessionPage(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Parking Sessions"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Admin view: filter, inspect sessions, and quickly end parking"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        top.setOpaque(false);

        top.add(new JLabel("Vehicle:"));
        top.add(vehicleField);

        top.add(new JLabel("Phone:"));
        top.add(phoneField);

        top.add(new JLabel("Lot:"));
        top.add(lotField);

        activeOnly.setOpaque(false);
        top.add(activeOnly);

        JButton search = MainFrame.primaryButton("Search");
        JButton refresh = MainFrame.secondaryButton("Refresh");
        JButton clear = MainFrame.secondaryButton("Clear");
        JButton endSelected = MainFrame.secondaryButton("End parking (selected)");
        JButton back = MainFrame.secondaryButton("Back");

        search.addActionListener(e -> doSearch());
        refresh.addActionListener(e -> doSearch());
        clear.addActionListener(e -> clearFilters());
        endSelected.addActionListener(e -> endSelected());
        back.addActionListener(e -> frame.showDashboard());

        top.add(search);
        top.add(refresh);
        top.add(clear);
        top.add(endSelected);
        top.add(back);

        root.add(top, BorderLayout.CENTER);

        model = new DefaultTableModel(new Object[]{
                "SessionID", "Vehicle", "Client", "Phone",
                "Lot", "City", "Start", "End",
                "SpaceID", "ConveyorID", "Active"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(24);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Sessions"));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        bottom.add(status, BorderLayout.WEST);

        JPanel centerWrap = new JPanel(new BorderLayout());
        centerWrap.setOpaque(false);
        centerWrap.add(tablePanel, BorderLayout.CENTER);
        centerWrap.add(bottom, BorderLayout.SOUTH);

        root.add(centerWrap, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    private void doSearch() {
        model.setRowCount(0);
        status.setText("Searching...");

        String vehicle = safe(vehicleField.getText());
        String phone = safe(phoneField.getText());
        Integer lot = parseIntOrNull(lotField.getText());

        try {
            List<ParkingSessionViewRow> rows =
                    control.search(vehicle, phone, lot, activeOnly.isSelected());

            for (ParkingSessionViewRow r : rows) {
                model.addRow(new Object[]{
                        r.sessionId,
                        r.vehicleNumber,
                        r.clientName,
                        r.phone,
                        r.lotNumber + (r.lotName != null ? " - " + r.lotName : ""),
                        r.city,
                        r.startTime,
                        r.endTime,
                        r.finalSpaceId,
                        r.currentConveyorId,
                        r.active ? "YES" : "NO"
                });
            }

            status.setText("Found " + rows.size() + " session(s).");
            if (model.getRowCount() > 0) table.setRowSelectionInterval(0, 0);

        } catch (Exception ex) {
            status.setText("Search failed: " + ex.getMessage());
        }
    }

    private void clearFilters() {
        vehicleField.setText("");
        phoneField.setText("");
        lotField.setText("");
        activeOnly.setSelected(true);
        model.setRowCount(0);
        status.setText(" ");
    }

    private void endSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a row first.");
            return;
        }
        String vehicle = String.valueOf(model.getValueAt(row, 1));
        String phone = String.valueOf(model.getValueAt(row, 3));

        // jump to your existing End Parking page with prefilled values:
        frame.openExitParking(vehicle, phone);
    }

    private String safe(String s) {
        if (s == null) return "";
        s = s.trim();
        return s.isEmpty() ? "" : s;
    }

    private Integer parseIntOrNull(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;
        try { return Integer.parseInt(s); }
        catch (Exception e) { return null; }
    }
}
