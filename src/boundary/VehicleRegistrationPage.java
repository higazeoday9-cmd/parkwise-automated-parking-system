package boundary;

import control.*;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VehicleRegistrationPage extends JPanel {

    private final MainFrame mainFrame;

    private final RegistrationControl registrationControl = new RegistrationControl();
    private final PhotoToTextService extractor = new PhotoToTextService();

    private int lotNumber;
    private String scannedVehicleNumber;

    private final JTextField firstNameField = new JTextField(12);
    private final JTextField lastNameField = new JTextField(12);
    private final JTextField phoneField = new JTextField(12);

    private File frontPhoto;
    private File rearPhoto;
    private File sidePhoto;

    private final JLabel frontLabel = new JLabel("No file selected");
    private final JLabel rearLabel = new JLabel("No file selected");
    private final JLabel sideLabel = new JLabel("No file selected");

    // “Mock extraction inputs” (still matches spec: auto extract, but we simulate)
    private final JTextField typeField = new JTextField(12);
    private final JComboBox<String> colorBox = new JComboBox<>(new String[]{"white", "black", "silver", "blue", "other"});
    private final JComboBox<String> sizeBox = new JComboBox<>(new String[]{"small", "medium", "large"});
    private final JTextField weightField = new JTextField("1200", 6);

    private final JTextArea output = new JTextArea(7, 45);

    public VehicleRegistrationPage(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Vehicle Registration (Barrier Didn't Open)");
        title.setFont(new Font("Arial", Font.BOLD, 18));

        JPanel form = buildForm();

        output.setEditable(false);
        output.setLineWrap(true);
        output.setWrapStyleWord(true);

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(new JScrollPane(output), BorderLayout.SOUTH);
    }

    public void setContext(int lotNumber, String scannedVehicleNumber) {
        this.lotNumber = lotNumber;
        this.scannedVehicleNumber = scannedVehicleNumber;

        output.setText("");
        output.append("Context: lotNumber=" + lotNumber + ", scannedVehicleNumber=" + scannedVehicleNumber + "\n");
        typeField.setText("");
        weightField.setText("1200");
        colorBox.setSelectedItem("other");
        sizeBox.setSelectedItem("medium");
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Client fields
        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("First Name:"), gc);
        gc.gridx = 1; panel.add(firstNameField, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Last Name:"), gc);
        gc.gridx = 1; panel.add(lastNameField, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Mobile Phone:"), gc);
        gc.gridx = 1; panel.add(phoneField, gc);
        row++;

        // Photos
        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Front Photo:"), gc);
        gc.gridx = 1; panel.add(frontLabel, gc);
        gc.gridx = 2; panel.add(btn("Choose", () -> choosePhoto(1)), gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Rear Photo:"), gc);
        gc.gridx = 1; panel.add(rearLabel, gc);
        gc.gridx = 2; panel.add(btn("Choose", () -> choosePhoto(2)), gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Side Photo:"), gc);
        gc.gridx = 1; panel.add(sideLabel, gc);
        gc.gridx = 2; panel.add(btn("Choose", () -> choosePhoto(3)), gc);
        row++;

        // Mock extraction inputs
        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Vehicle Type (text):"), gc);
        gc.gridx = 1; panel.add(typeField, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Color:"), gc);
        gc.gridx = 1; panel.add(colorBox, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Size:"), gc);
        gc.gridx = 1; panel.add(sizeBox, gc);
        row++;

        gc.gridx = 0; gc.gridy = row; panel.add(new JLabel("Weight (kg):"), gc);
        gc.gridx = 1; panel.add(weightField, gc);
        row++;

        JButton extractBtn = new JButton("Extract (Mock Photo-to-Text)");
        extractBtn.addActionListener(e -> onExtract());

        JButton saveBtn = new JButton("Save + Open Barrier");
        saveBtn.addActionListener(e -> onSave());

        JButton backBtn = new JButton("Back to Dashboard");
        backBtn.addActionListener(e -> mainFrame.showDashboard());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(extractBtn);
        buttons.add(saveBtn);
        buttons.add(backBtn);

        gc.gridx = 0; gc.gridy = row; gc.gridwidth = 3;
        panel.add(buttons, gc);

        return panel;
    }

    private void onExtract() {
        output.setText("");

        if (frontPhoto == null || rearPhoto == null || sidePhoto == null) {
            output.append("Please choose 3 photos (front, rear, side) first.\n");
            return;
        }

        VehicleExtractionResult extracted = extractor.extract(
                scannedVehicleNumber,
                frontPhoto, rearPhoto, sidePhoto,
                typeField.getText(),
                (String) colorBox.getSelectedItem(),
                (String) sizeBox.getSelectedItem(),
                weightField.getText()
        );

        output.append("Extracted Vehicle Details:\n");
        output.append("Number: " + extracted.getVehicleNumber() + "\n");
        output.append("Type: " + extracted.getType() + "\n");
        output.append("Color: " + extracted.getColor() + "\n");
        output.append("Size: " + extracted.getSize() + "\n");
        output.append("WeightKg: " + extracted.getWeightKg() + "\n");
    }

    private void onSave() {
        output.setText("");

        if (frontPhoto == null || rearPhoto == null || sidePhoto == null) {
            output.append("Please choose 3 photos (front, rear, side) first.\n");
            return;
        }

        VehicleExtractionResult extracted = extractor.extract(
                scannedVehicleNumber,
                frontPhoto, rearPhoto, sidePhoto,
                typeField.getText(),
                (String) colorBox.getSelectedItem(),
                (String) sizeBox.getSelectedItem(),
                weightField.getText()
        );

        ArrivalResult result = registrationControl.registerAndArrive(
                lotNumber,
                firstNameField.getText(),
                lastNameField.getText(),
                phoneField.getText(),
                extracted
        );

        output.append(result.getMessage() + "\n");

        if (result.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                    "Barrier opened and session created.\nSession ID: " + result.getCreatedSessionId(),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.showDashboard();
        }
    }

    private void choosePhoto(int which) {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        File f = chooser.getSelectedFile();
        if (which == 1) { frontPhoto = f; frontLabel.setText(f.getName()); }
        if (which == 2) { rearPhoto = f; rearLabel.setText(f.getName()); }
        if (which == 3) { sidePhoto = f; sideLabel.setText(f.getName()); }
    }

    private JButton btn(String text, Runnable r) {
        JButton b = new JButton(text);
        b.addActionListener(e -> r.run());
        return b;
    }
}

