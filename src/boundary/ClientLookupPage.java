package boundary;

import control.ActiveSessionView;
import control.ClientLookupControl;
import control.ClientLookupResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ClientLookupPage extends JPanel {

    private final MainFrame frame;
    private final ClientLookupControl control = new ClientLookupControl();

    private final JTextField phoneField = new JTextField(16);

    private final JLabel clientLbl = MainFrame.subtitle(" ");
    private final JLabel statusLbl = MainFrame.subtitle(" ");

    private final DefaultTableModel model;
    private final JTable table;

    private String lastPhoneNormalized = null;

    public ClientLookupPage(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        // ---------- header ----------
        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Client Lookup"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Enter phone number to view active parking details"));
        MainFrame.space(header, 10);
        header.add(clientLbl);
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        // ---------- center panel (form + table) ----------
        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        form.setOpaque(false);

        form.add(new JLabel("Phone:"));
        form.add(phoneField);

        JButton search = MainFrame.primaryButton("Search");
        JButton endSelected = MainFrame.secondaryButton("End parking (selected)");
        JButton back = MainFrame.secondaryButton("Back to Dashboard");

        search.addActionListener(e -> doSearch());
        endSelected.addActionListener(e -> endSelected());
        back.addActionListener(e -> frame.showDashboard());

        form.add(search);
        form.add(endSelected);
        form.add(back);

        center.add(form, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"Vehicle", "SessionID", "Lot", "City", "StartTime", "Location"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(24);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder("Active Sessions"));
        center.add(sp, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        // ---------- status footer ----------
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        footer.add(statusLbl, BorderLayout.WEST);
        root.add(footer, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    // Optional: allow MainFrame to prefill phone and auto-search
    public void setContext(String phone, boolean autoSearch) {
        phoneField.setText(phone == null ? "" : phone);
        if (autoSearch) SwingUtilities.invokeLater(this::doSearch);
    }

    private void doSearch() {
        model.setRowCount(0);
        clientLbl.setText(" ");
        statusLbl.setText("Searching...");

        ClientLookupResult r = control.lookupByPhone(phoneField.getText());
        if (!r.success) {
            statusLbl.setText(r.message);
            return;
        }

        lastPhoneNormalized = r.phone;

        clientLbl.setText(r.clientFullName + " — " + r.phone);
        statusLbl.setText(r.message);

        for (ActiveSessionView s : r.activeSessions) {
            model.addRow(new Object[]{
                    s.vehicleNumber,
                    s.sessionId,
                    s.lotNumber + (s.lotName != null ? " - " + s.lotName : ""),
                    s.city == null ? "" : s.city,
                    s.startTime,
                    s.locationText
            });
        }

        if (model.getRowCount() == 1) table.setRowSelectionInterval(0, 0);
    }

    private void endSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a session row first.");
            return;
        }
        String vehicle = String.valueOf(model.getValueAt(row, 0));
        String phone = (lastPhoneNormalized != null) ? lastPhoneNormalized : phoneField.getText().trim();
        frame.openExitParking(vehicle, phone);
    }
}
