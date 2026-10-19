package boundary;

import control.EndParkingControl;
import control.EndParkingResult;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ExitParkingPage extends JPanel {

    private final MainFrame frame;
    private final EndParkingControl control = new EndParkingControl();

    private final JTextField vehicleField = new JTextField(14);
    private final JTextField phoneField = new JTextField(14);
    private final JCheckBox receiptCb = new JCheckBox("Generate receipt text");

    private final JTextArea out = new JTextArea(12, 60);

    public ExitParkingPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("End Parking & Payment"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Validate phone, calculate payment, approve via PG stub, open gate"));
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
        form.add(vehicleField, gc);

        gc.gridx = 0; gc.gridy = 1;
        form.add(new JLabel("Phone (validation):"), gc);
        gc.gridx = 1;
        form.add(phoneField, gc);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        receiptCb.setOpaque(false);
        receiptCb.setSelected(true);
        form.add(receiptCb, gc);

        JButton pay = MainFrame.primaryButton("End Parking + Pay");
        JButton copyReceipt = MainFrame.secondaryButton("Copy receipt");
        JButton back = MainFrame.secondaryButton("Back");

        pay.addActionListener(e -> onPay());
        copyReceipt.addActionListener(e -> copyReceipt());
        back.addActionListener(e -> frame.showDashboard());

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        btns.setOpaque(false);
        btns.add(pay);
        btns.add(Box.createRigidArea(new Dimension(12, 0)));
        btns.add(copyReceipt);
        btns.add(Box.createRigidArea(new Dimension(12, 0)));
        btns.add(back);
        form.add(btns, gc);

        root.add(form, BorderLayout.CENTER);

        out.setEditable(false);
        out.setLineWrap(true);
        out.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(out);
        sp.setBorder(BorderFactory.createTitledBorder("Result"));
        root.add(sp, BorderLayout.SOUTH);

        add(root, BorderLayout.CENTER);
    }

    public void setContext(String vehicleNumber, String phone) {
        vehicleField.setText(vehicleNumber == null ? "" : vehicleNumber);
        phoneField.setText(phone == null ? "" : phone);
    }

    private void onPay() {
        out.setText("");
        EndParkingResult r = control.endParking(vehicleField.getText(), phoneField.getText(), receiptCb.isSelected());

        out.append(r.message + "\n\n");
        if (!r.success) return;

        out.append("SessionID: " + r.sessionId + "\n");
        out.append("Hours: " + r.totalHours + "\n");
        out.append("Amount before discount: " + r.amountBeforeDiscount + "\n");
        out.append("Final amount: " + r.amountFinal + "\n");
        out.append("Transaction: " + r.transactionId + "\n\n");

        if (r.receiptText != null) out.append(r.receiptText);
    }

    private void copyReceipt() {
        String text = out.getText();
        if (text == null || text.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to copy yet.");
            return;
        }
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        JOptionPane.showMessageDialog(this, "Copied to clipboard.");
    }
}

