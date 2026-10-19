package boundary;

import control.CustomerClubUIControl;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class CustomerClubPage extends JPanel {

    private final MainFrame frame;
    private final CustomerClubUIControl clubUI = new CustomerClubUIControl();

    // UI
    private JTextField phoneField;
    private JButton loadBtn;
    private JButton registerBtn;
    private JLabel statusLbl;

    private JPanel lotsPanel;
    private final Map<Integer, JCheckBox> lotChecks = new LinkedHashMap<>();
    private JButton savePreferredBtn;

    private JComboBox<Integer> yearCombo;
    private JButton buildGrowthBtn;
    private JTable growthTable;
    private DefaultTableModel growthModel;

    // Current loaded client
    private CustomerClubUIControl.ClientInfo currentClient = null;

    public CustomerClubPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();
        add(root, BorderLayout.CENTER);

        JPanel content = MainFrame.centerBox();
        root.add(content, BorderLayout.CENTER);

        // Header
        content.add(MainFrame.title("Customer Club"));
        MainFrame.space(content, 6);
        content.add(MainFrame.subtitle("Register members, manage preferred lots, and view club growth"));
        MainFrame.space(content, 10);
        content.add(MainFrame.niceLine());
        MainFrame.space(content, 16);

        // Section: Load / Register
        content.add(buildLoadRegisterSection());
        MainFrame.space(content, 16);

        // Section: Preferred lots
        content.add(buildPreferredLotsSection());
        MainFrame.space(content, 18);

        // Section: Growth report
        content.add(buildGrowthSection());
        MainFrame.space(content, 10);

        setEnabledState(false);

        // Preload lots list once (checkboxes exist even before loading member)
        reloadLotsCheckboxes(Collections.emptySet());
    }

    private JPanel buildLoadRegisterSection() {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        JLabel phoneLbl = new JLabel("Client phone:");
        phoneLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));

        phoneField = new JTextField(16);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        loadBtn = MainFrame.primaryButton("Load");
        registerBtn = MainFrame.secondaryButton("Register membership");

        statusLbl = new JLabel("No client loaded.");
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusLbl.setForeground(new Color(90, 90, 90));

        gc.gridx = 0; gc.gridy = 0;
        form.add(phoneLbl, gc);

        gc.gridx = 1; gc.gridy = 0; gc.fill = GridBagConstraints.HORIZONTAL; gc.weightx = 1;
        form.add(phoneField, gc);

        gc.gridx = 2; gc.gridy = 0; gc.fill = GridBagConstraints.NONE; gc.weightx = 0;
        form.add(loadBtn, gc);

        gc.gridx = 3; gc.gridy = 0;
        form.add(registerBtn, gc);

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 4;
        form.add(statusLbl, gc);

        box.add(form, BorderLayout.CENTER);

        // Actions
        loadBtn.addActionListener(e -> onLoadClient());
        registerBtn.addActionListener(e -> onRegisterMembership());

        return box;
    }

    private JPanel buildPreferredLotsSection() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);

        JLabel lbl = new JLabel("Preferred parking lots (check to select):");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        wrap.add(lbl, BorderLayout.NORTH);

        lotsPanel = new JPanel();
        lotsPanel.setOpaque(false);
        lotsPanel.setLayout(new BoxLayout(lotsPanel, BoxLayout.Y_AXIS));
        lotsPanel.setBorder(new EmptyBorder(6, 6, 6, 6));

        JScrollPane sp = new JScrollPane(lotsPanel);
        sp.setPreferredSize(new Dimension(700, 180));
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        wrap.add(sp, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(8, 0, 0, 0));

        savePreferredBtn = MainFrame.primaryButton("Save preferred lots");
        bottom.add(savePreferredBtn);
        wrap.add(bottom, BorderLayout.SOUTH);

        savePreferredBtn.addActionListener(e -> onSavePreferredLots());

        return wrap;
    }

    private JPanel buildGrowthSection() {
        JPanel wrap = new JPanel(new BorderLayout(0, 8));
        wrap.setOpaque(false);

        // Title
        JLabel lbl = new JLabel("Customer Club Growth Report");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        wrap.add(lbl, BorderLayout.NORTH);

        // Control bar (Year + Button)
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        controlBar.setOpaque(false);

        JLabel yrLbl = new JLabel("Year:");
        yrLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        controlBar.add(yrLbl);

        int y = LocalDate.now().getYear();
        yearCombo = new JComboBox<>();
        for (int yr = y - 3; yr <= y; yr++) yearCombo.addItem(yr);
        yearCombo.setSelectedItem(y);
        yearCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        buildGrowthBtn = MainFrame.secondaryButton("Build report");
        buildGrowthBtn.addActionListener(e -> onBuildGrowth());

        controlBar.add(yearCombo);
        controlBar.add(buildGrowthBtn);

        // Table
        growthModel = new DefaultTableModel(new Object[]{"Month", "Joined", "Total (cumulative)", "Growth %"}, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        growthTable = new JTable(growthModel);
        growthTable.setRowHeight(24);
        growthTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        growthTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane sp = new JScrollPane(growthTable);
        sp.setPreferredSize(new Dimension(700, 200));
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        // Assemble into a clean vertical stack
        JPanel centerPanel = new JPanel(new BorderLayout(0, 6));
        centerPanel.setOpaque(false);
        centerPanel.add(controlBar, BorderLayout.NORTH);
        centerPanel.add(sp, BorderLayout.CENTER);

        wrap.add(centerPanel, BorderLayout.CENTER);

        return wrap;
    }

    private void setEnabledState(boolean loaded) {
        registerBtn.setEnabled(loaded);
        savePreferredBtn.setEnabled(loaded);
    }

    private void onLoadClient() {
        try {
            String phone = phoneField.getText().trim();
            if (phone.isEmpty()) {
                showError("Please enter a phone number.");
                return;
            }

            currentClient = clubUI.findClientByPhone(phone);
            if (currentClient == null) {
                setEnabledState(false);
                statusLbl.setText("Client not found for phone: " + phone);
                reloadLotsCheckboxes(Collections.emptySet());
                return;
            }

            boolean member = clubUI.isMember(currentClient.clientId);

            statusLbl.setText("Loaded: " + safeName(currentClient) +
                    " (ClientID: " + currentClient.clientId + ") — " +
                    (member ? "Membership: ACTIVE" : "Membership: NOT registered"));

            Set<Integer> preferred = member ? clubUI.getPreferredLots(currentClient.clientId) : Collections.emptySet();
            reloadLotsCheckboxes(preferred);

            setEnabledState(true);

        } catch (Exception ex) {
            setEnabledState(false);
            showError("Load failed: " + ex.getMessage());
        }
    }

    private void onRegisterMembership() {
        if (currentClient == null) {
            showError("Load a client first.");
            return;
        }

        try {
            clubUI.registerMemberIfMissing(currentClient.clientId);

            boolean member = clubUI.isMember(currentClient.clientId);
            statusLbl.setText("Loaded: " + safeName(currentClient) +
                    " (ClientID: " + currentClient.clientId + ") — " +
                    (member ? "Membership: ACTIVE" : "Membership: NOT registered"));

            // After registering, reload preferred lots (should exist now)
            Set<Integer> preferred = clubUI.getPreferredLots(currentClient.clientId);
            reloadLotsCheckboxes(preferred);

        } catch (Exception ex) {
            showError("Registration failed: " + ex.getMessage());
        }
    }

    private void onSavePreferredLots() {
        if (currentClient == null) {
            showError("Load a client first.");
            return;
        }

        try {
            if (!clubUI.isMember(currentClient.clientId)) {
                showError("This client is not a club member yet. Click 'Register membership' first.");
                return;
            }

            Set<Integer> chosen = new HashSet<>();
            for (Map.Entry<Integer, JCheckBox> e : lotChecks.entrySet()) {
                if (e.getValue().isSelected()) chosen.add(e.getKey());
            }

            clubUI.savePreferredLots(currentClient.clientId, chosen);
            showInfo("Saved preferred lots (" + chosen.size() + ").");

        } catch (Exception ex) {
            showError("Save failed: " + ex.getMessage());
        }
    }

    private void onBuildGrowth() {
        try {
            int year = (Integer) yearCombo.getSelectedItem();
            List<CustomerClubUIControl.GrowthRow> rows = clubUI.buildGrowthReport(year);

            growthModel.setRowCount(0);
            for (CustomerClubUIControl.GrowthRow r : rows) {
                growthModel.addRow(new Object[]{
                        monthName(r.month),
                        r.joined,
                        r.total,
                        r.growthPct
                });
            }

        } catch (Exception ex) {
            showError("Growth report failed: " + ex.getMessage());
        }
    }

    private void reloadLotsCheckboxes(Set<Integer> selected) {
        try {
            lotsPanel.removeAll();
            lotChecks.clear();

            java.util.List<CustomerClubUIControl.LotInfo> lots = clubUI.getAllLots();
            if (lots.isEmpty()) {
                JLabel none = new JLabel("No parking lots found in DB.");
                none.setForeground(new Color(130, 130, 130));
                lotsPanel.add(none);
            } else {
                for (CustomerClubUIControl.LotInfo li : lots) {
                    JCheckBox cb = new JCheckBox(li.toString());
                    cb.setOpaque(false);
                    cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    cb.setSelected(selected.contains(li.lotNumber));

                    lotChecks.put(li.lotNumber, cb);
                    lotsPanel.add(cb);
                }
            }

            lotsPanel.revalidate();
            lotsPanel.repaint();

        } catch (Exception ex) {
            // If lots fail to load, still keep UI usable
            lotsPanel.removeAll();
            JLabel err = new JLabel("Failed to load lots: " + ex.getMessage());
            err.setForeground(new Color(170, 60, 60));
            lotsPanel.add(err);
            lotsPanel.revalidate();
            lotsPanel.repaint();
        }
    }

    // -------------------- Small helpers --------------------

    private static String safeName(CustomerClubUIControl.ClientInfo c) {
        String fn = (c.firstName == null) ? "" : c.firstName.trim();
        String ln = (c.lastName == null) ? "" : c.lastName.trim();
        String full = (fn + " " + ln).trim();
        return full.isBlank() ? ("Client " + c.clientId) : full;
    }

    private static String monthName(int m) {
        return switch (m) {
            case 1 -> "Jan";
            case 2 -> "Feb";
            case 3 -> "Mar";
            case 4 -> "Apr";
            case 5 -> "May";
            case 6 -> "Jun";
            case 7 -> "Jul";
            case 8 -> "Aug";
            case 9 -> "Sep";
            case 10 -> "Oct";
            case 11 -> "Nov";
            case 12 -> "Dec";
            default -> String.valueOf(m);
        };
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
}

