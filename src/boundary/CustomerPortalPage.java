package boundary;

import control.ActiveSessionView;
import control.BillingCalculator;
import control.ClientLookupControl;
import control.ClientLookupResult;
import control.CustomerClubPricingControl;
import control.EndParkingControl;
import control.EndParkingResult;
import entity.DB;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;

public class CustomerPortalPage extends JPanel {

    private final MainFrame frame;
    private final ClientLookupControl lookupControl = new ClientLookupControl();
    private final BillingCalculator calc = new BillingCalculator();
    private final EndParkingControl endParkingControl = new EndParkingControl();

    private final JLabel welcomeLbl = MainFrame.title("My Parking Session");
    private final JLabel clientInfoLbl = MainFrame.subtitle(" ");

    // Session cards & details
    private final JPanel sessionContainer = new JPanel();
    private final JTextArea receiptOut = new JTextArea(10, 50);

    private String currentPhone = null;
    private ActiveSessionView currentSession = null;
    private int currentEstimatedFee = 0;

    public CustomerPortalPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        // Header
        JPanel header = MainFrame.centerBox();
        header.add(welcomeLbl);
        MainFrame.space(header, 4);
        header.add(clientInfoLbl);
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        // Center: Session Details Panel
        sessionContainer.setOpaque(false);
        sessionContainer.setLayout(new BoxLayout(sessionContainer, BoxLayout.Y_AXIS));

        JScrollPane centerScroll = new JScrollPane(sessionContainer);
        centerScroll.setBorder(null);
        centerScroll.getViewport().setOpaque(false);
        root.add(centerScroll, BorderLayout.CENTER);

        // Footer Receipt output area
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        receiptOut.setEditable(false);
        receiptOut.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane sp = new JScrollPane(receiptOut);
        sp.setBorder(BorderFactory.createTitledBorder("Receipt / Exit Confirmation"));
        sp.setPreferredSize(new Dimension(0, 160));
        footer.add(sp, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnBar.setOpaque(false);
        JButton copyBtn = MainFrame.secondaryButton("Copy Receipt");
        copyBtn.addActionListener(e -> {
            String t = receiptOut.getText();
            if (t != null && !t.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(t), null);
                JOptionPane.showMessageDialog(this, "Receipt copied to clipboard.");
            }
        });
        btnBar.add(copyBtn);
        footer.add(btnBar, BorderLayout.SOUTH);

        root.add(footer, BorderLayout.SOUTH);
        add(root, BorderLayout.CENTER);
    }

    public void loadCustomer(String phone) {
        this.currentPhone = phone;
        receiptOut.setText("");
        sessionContainer.removeAll();

        ClientLookupResult res = lookupControl.lookupByPhone(phone);
        if (!res.success || res.activeSessions.isEmpty()) {
            clientInfoLbl.setText(res.clientFullName != null ? res.clientFullName + " (" + phone + ")" : phone);

            JPanel emptyBox = new JPanel();
            emptyBox.setOpaque(false);
            JLabel msg = new JLabel("No active parking sessions found. Your vehicle is not currently parked.");
            msg.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            msg.setForeground(new Color(120, 120, 120));
            emptyBox.add(msg);
            sessionContainer.add(emptyBox);
            sessionContainer.revalidate();
            sessionContainer.repaint();
            return;
        }

        clientInfoLbl.setText("Welcome back, " + res.clientFullName + " (" + phone + ")");
        currentSession = res.activeSessions.get(0);

        // Render Active Parking Session Card
        sessionContainer.add(buildActiveSessionCard(currentSession));
        sessionContainer.revalidate();
        sessionContainer.repaint();
    }

    private JPanel buildActiveSessionCard(ActiveSessionView s) {
        JPanel card = new JPanel();
        card.setLayout(new GridBagLayout());
        card.setBackground(new Color(250, 252, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 210, 245), 1),
                BorderFactory.createEmptyBorder(20, 24, 20, 24)
        ));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 12, 6, 12);
        gc.anchor = GridBagConstraints.WEST;

        LocalDateTime start = s.startTime.toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        Duration dur = Duration.between(start, now);
        long hours = Math.max(1, (long) Math.ceil(dur.toMinutes() / 60.0));

        int estimatedFee = calculateEstimatedFee(s.lotNumber, dur.toMinutes());
        this.currentEstimatedFee = estimatedFee;

        // Vehicle Plate & Lot
        gc.gridx = 0; gc.gridy = 0;
        JLabel vLbl = new JLabel("Vehicle Plate:");
        vLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(vLbl, gc);
        gc.gridx = 1;
        JLabel vVal = new JLabel(s.vehicleNumber);
        vVal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        vVal.setForeground(new Color(20, 80, 160));
        card.add(vVal, gc);

        // Facility & Location
        gc.gridx = 0; gc.gridy = 1;
        card.add(new JLabel("Parking Facility:"), gc);
        gc.gridx = 1;
        card.add(new JLabel(s.lotNumber + " - " + (s.lotName != null ? s.lotName : "") + " (" + s.city + ")"), gc);

        gc.gridx = 0; gc.gridy = 2;
        card.add(new JLabel("Assigned Position:"), gc);
        gc.gridx = 1;
        card.add(new JLabel(s.locationText), gc);

        // Start Time & Duration
        gc.gridx = 0; gc.gridy = 3;
        card.add(new JLabel("Session Started:"), gc);
        gc.gridx = 1;
        card.add(new JLabel(s.startTime.toString()), gc);

        gc.gridx = 0; gc.gridy = 4;
        card.add(new JLabel("Elapsed Duration:"), gc);
        gc.gridx = 1;
        card.add(new JLabel(dur.toHoursPart() + "h " + dur.toMinutesPart() + "m (" + hours + " billed hour/s)"), gc);

        // Accumulated Amount
        gc.gridx = 0; gc.gridy = 5;
        JLabel aLbl = new JLabel("Accumulated Amount:");
        aLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        card.add(aLbl, gc);
        gc.gridx = 1;
        JLabel aVal = new JLabel(estimatedFee + " ILS");
        aVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        aVal.setForeground(new Color(30, 130, 60));
        card.add(aVal, gc);

        // Pay & End Parking Button
        JButton payBtn = MainFrame.primaryButton("End Parking & Pay Now");
        payBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        payBtn.addActionListener(e -> onEndParkingAndPay(s.vehicleNumber));

        gc.gridx = 0; gc.gridy = 6; gc.gridwidth = 2;
        gc.insets = new Insets(18, 12, 0, 12);
        card.add(payBtn, gc);

        return card;
    }

    private int calculateEstimatedFee(int lotNumber, long minutes) {
        int year = LocalDateTime.now().getYear();
        try (Connection c = DB.getConnection()) {
            String sql = "SELECT TOP 1 priceFirstHour, priceAdditionalHour, priceFullDay " +
                    "FROM PriceRate WHERE lotNumber=? AND effectiveYear<=? ORDER BY effectiveYear DESC";
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setInt(1, lotNumber);
                ps.setInt(2, year);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int f = rs.getInt("priceFirstHour");
                        int a = rs.getInt("priceAdditionalHour");
                        int d = rs.getInt("priceFullDay");
                        return calc.calculate(minutes, f, a, d).amount;
                    }
                }
            }
        } catch (Exception ignored) {}
        return 20; // fallback default
    }

    private void onEndParkingAndPay(String vehicleNumber) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm checkout and pay for vehicle " + vehicleNumber + "?\nEstimated Amount: " + currentEstimatedFee + " ILS",
                "Confirm Checkout",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        EndParkingResult res = endParkingControl.endParking(vehicleNumber, currentPhone, true);
        if (!res.success) {
            JOptionPane.showMessageDialog(this, res.message, "Payment Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        receiptOut.setText(res.receiptText);
        JOptionPane.showMessageDialog(
                this,
                "Payment Approved (" + res.amountFinal + " ILS)!\nExit barrier opened. Have a safe drive!",
                "Exit Confirmed",
                JOptionPane.INFORMATION_MESSAGE
        );

        // Reload customer view -> session is now ended
        loadCustomer(currentPhone);
    }
}
