package boundary;

import control.ParkingEntryControlAdapter;
import control.VehicleClientInfoControl;
import entity.DB;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class GateSimulatorPage extends JPanel {

    private final MainFrame frame;

    private final JTextField lotField = new JTextField(10);
    private final JTextField vehicleField = new JTextField(14);

    private final JTextArea output = new JTextArea(12, 70);

    public GateSimulatorPage(MainFrame frame) {
        this.frame = frame;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Gate Sensor Simulator (Arrival)"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Simulate vehicle arrival (GS)"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        form.add(new JLabel("Lot Number:"), gc);
        gc.gridx = 1;
        form.add(lotField, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(new JLabel("Scanned Vehicle Number:"), gc);
        gc.gridx = 1;
        form.add(vehicleField, gc);

        JButton arrive = MainFrame.primaryButton("Arrive (GS Event)");
        JButton clear = MainFrame.secondaryButton("Clear Output");

        arrive.addActionListener(e -> onArrive());
        clear.addActionListener(e -> output.setText(""));

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btns.setOpaque(false);
        btns.add(arrive);
        btns.add(Box.createRigidArea(new Dimension(12, 0)));
        btns.add(clear);
        form.add(btns, gc);

        root.add(form, BorderLayout.CENTER);

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(output);
        sp.setBorder(BorderFactory.createTitledBorder("Output"));
        root.add(sp, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);

        if (lotField.getText().isBlank()) lotField.setText("1");
        if (vehicleField.getText().isBlank()) vehicleField.setText("1001");
    }

    private void onArrive() {
        int lot;
        String vehicle = vehicleField.getText() == null ? "" : vehicleField.getText().trim();

        try {
            lot = Integer.parseInt(lotField.getText().trim());
        } catch (Exception e) {
            append("Arrival failed: invalid lot number.");
            return;
        }

        if (vehicle.isEmpty()) {
            append("Arrival failed: vehicle number is required.");
            return;
        }

        append("=== ARRIVAL EVENT ===");
        append("Lot=" + lot + ", Vehicle=" + vehicle);

        // 1) DB facts BEFORE calling ParkingEntryControl
        boolean existsBefore = vehicleExists(vehicle);
        VehicleClientInfoControl.ClientInfo clientBefore = findClientSafe(vehicle);

        append("[DB] Vehicle exists? " + (existsBefore ? "YES" : "NO"));
        append("[DB] Linked to client? " + (clientBefore != null ? "YES" : "NO"));

        // 2) Call ParkingEntryControl (through adapter)
        ParkingEntryControlAdapter.ArrivalResult r;
        try {
            r = new ParkingEntryControlAdapter().simulateArrival(lot, vehicle);
            if (r.message != null && !r.message.isBlank()) {
                append("[ParkingEntryControl] " + r.message);
            }
        } catch (Exception ex) {
            append("[ParkingEntryControl] Exception: " + ex.getMessage());
            // even if ParkingEntryControl failed, we still decide based on DB facts
            r = new ParkingEntryControlAdapter.ArrivalResult();
            r.success = false;
            r.barrierOpened = false;
            r.message = ex.getMessage();
        }

        // 3) DB facts AFTER calling ParkingEntryControl
        boolean existsAfter = vehicleExists(vehicle);
        VehicleClientInfoControl.ClientInfo clientAfter = findClientSafe(vehicle);

        append("[DB after] Vehicle exists? " + (existsAfter ? "YES" : "NO"));
        append("[DB after] Linked to client? " + (clientAfter != null ? "YES" : "NO"));

        // 4) Decide: when to go to registration?
        boolean explicitBarrierFailure = looksLikeBarrierFailure(r.message);

        // If vehicle is not even in DB => must register
        if (!existsAfter) {
            append("➡ Vehicle not found in DB -> going to registration.");
            frame.openVehicleRegistration(lot, vehicle);
            return;
        }

        // Vehicle exists but not linked => registration is needed to attach client
        if (clientAfter == null) {
            append("➡ Vehicle exists but NOT linked to a client -> going to registration to attach client.");
            frame.openVehicleRegistration(lot, vehicle);
            return;
        }

        // If barrier explicitly failed (rare) -> follow spec instructions page (registration flow)
        if (explicitBarrierFailure) {
            append("➡ Barrier failure detected -> going to registration/instructions flow.");
            frame.openVehicleRegistration(lot, vehicle);
            return;
        }

        // Otherwise: success, print client info
        append("Arrival completed ✅");
        printClientInfo(clientAfter);
    }

    private VehicleClientInfoControl.ClientInfo findClientSafe(String vehicleNumber) {
        try {
            return new VehicleClientInfoControl().findClientByVehicle(vehicleNumber);
        } catch (Exception ignored) {
            return null;
        }
    }

    private void printClientInfo(VehicleClientInfoControl.ClientInfo ci) {
        append("--- CLIENT INFO ---");
        append("ClientID: " + ci.clientId);
        append("Client: " + ci.fullName().trim());
        append("Phone: " + ci.phone);
        append("-------------------");
    }

    private boolean looksLikeBarrierFailure(String msg) {
        if (msg == null) return false;
        String m = msg.toLowerCase();
        if (!m.contains("barrier") && !m.contains("gate")) return false;
        return (m.contains("fail") || m.contains("did not") || m.contains("not open") || m.contains("couldn't"));
    }

    /**
     * Checks if the vehicle exists in DB even if it's not linked to a client yet.
     * Tries a couple of common table/column combos.
     */
    private boolean vehicleExists(String vehicleNumber) {
        // Try table Vehicle / Vehicles and columns vehicleNumber / number
        String[] tables = {"Vehicle", "Vehicles"};
        String[] cols = {"vehicleNumber", "number"};

        for (String t : tables) {
            for (String c : cols) {
                String sql = "SELECT COUNT(*) FROM " + t + " WHERE " + c + " = ?";
                try (Connection con = DB.getConnection();
                     PreparedStatement ps = con.prepareStatement(sql)) {

                    // if numeric value, try int first (works better if column is numeric)
                    try {
                        int n = Integer.parseInt(vehicleNumber);
                        ps.setInt(1, n);
                    } catch (Exception e) {
                        ps.setString(1, vehicleNumber);
                    }

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return rs.getInt(1) > 0;
                        }
                    }
                } catch (Exception ignored) {
                    // try next combo
                }
            }
        }
        return false;
    }

    private void append(String s) {
        output.append(s + "\n");
        output.setCaretPosition(output.getDocument().getLength());
    }
}

