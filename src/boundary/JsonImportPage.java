package boundary;

import control.IRSImportControl;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class JsonImportPage extends JPanel {

    private final IRSImportControl importControl = new IRSImportControl();
    private File selectedFile;

    public JsonImportPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("IRS JSON Import"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Import pricing rates from a JSON file into the DB"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        JLabel fileLbl = new JLabel("Selected file:");
        fileLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField fileTf = new JTextField(40);
        fileTf.setEditable(false);
        fileTf.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton choose = MainFrame.secondaryButton("Choose JSON");
        JButton importBtn = MainFrame.primaryButton("Import");

        JLabel status = MainFrame.subtitle(" ");

        c.gridx = 0; c.gridy = 0;
        body.add(fileLbl, c);

        c.gridx = 1; c.gridy = 0; c.gridwidth = 2;
        body.add(fileTf, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 1;
        body.add(choose, c);

        c.gridx = 1; c.gridy = 1;
        body.add(importBtn, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 3;
        body.add(status, c);

        choose.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Select IRS JSON file");
            int res = fc.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                selectedFile = fc.getSelectedFile();
                fileTf.setText(selectedFile.getAbsolutePath());
                status.setText("Ready to import.");
            }
        });

        importBtn.addActionListener(e -> {
            if (selectedFile == null) {
                JOptionPane.showMessageDialog(this, "Please choose a JSON file first.");
                return;
            }

            importBtn.setEnabled(false);
            status.setText("Importing...");

            SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return importControl.importPriceRates(selectedFile.getAbsolutePath());
                }

                @Override
                protected void done() {
                    importBtn.setEnabled(true);
                    try {
                        int inserted = get();
                        status.setText("Imported successfully.");
                        JOptionPane.showMessageDialog(JsonImportPage.this,
                                "Import completed.\nInserted rows: " + inserted,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception ex) {
                        status.setText("Failed.");
                        JOptionPane.showMessageDialog(JsonImportPage.this,
                                "Import error:\n" + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        root.add(body, BorderLayout.CENTER);

        JButton back = MainFrame.secondaryButton("Back");
        back.addActionListener(e -> frame.showPage(MainFrame.PAGE_DASHBOARD));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.add(back);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }
}

