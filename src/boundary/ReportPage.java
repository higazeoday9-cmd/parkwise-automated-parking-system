package boundary;

import control.ReportControl;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ReportPage extends JPanel {

    private final ReportControl reportControl = new ReportControl();

    public ReportPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        // Header
        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Generate Report"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Generate the Annual Summary Report (PDF)"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        // Body
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(8, 8, 8, 8);
        c.anchor = GridBagConstraints.WEST;

        JLabel yearLbl = new JLabel("Year:");
        yearLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JTextField yearTf = new JTextField(10);
        yearTf.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton gen = MainFrame.primaryButton("Generate PDF");

        c.gridx = 0; c.gridy = 0;
        body.add(yearLbl, c);

        c.gridx = 1; c.gridy = 0;
        body.add(yearTf, c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2;
        body.add(gen, c);

        JLabel status = MainFrame.subtitle(" ");
        c.gridx = 0; c.gridy = 2; c.gridwidth = 2;
        body.add(status, c);

        gen.addActionListener(e -> {
            String s = yearTf.getText().trim();
            if (!s.matches("\\d{4}")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid year (e.g., 2025).");
                return;
            }

            int year = Integer.parseInt(s);

            gen.setEnabled(false);
            status.setText("Generating report...");

            SwingWorker<File, Void> worker = new SwingWorker<>() {
                @Override
                protected File doInBackground() throws Exception {
                    return reportControl.generateAnnualSummaryPdf(year);
                }

                @Override
                protected void done() {
                    gen.setEnabled(true);
                    try {
                        File pdf = get();
                        status.setText("Done: " + pdf.getName());

                        int choice = JOptionPane.showConfirmDialog(
                                ReportPage.this,
                                "Report generated:\n" + pdf.getAbsolutePath() + "\n\nOpen it now?",
                                "Success",
                                JOptionPane.YES_NO_OPTION
                        );

                        if (choice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().open(pdf);
                        }
                    } catch (Exception ex) {
                        status.setText("Failed.");
                        JOptionPane.showMessageDialog(ReportPage.this,
                                "Error generating report:\n" + ex.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            };
            worker.execute();
        });

        root.add(body, BorderLayout.CENTER);

        // Footer
        JButton back = MainFrame.secondaryButton("Back");
        back.addActionListener(e -> frame.showPage(MainFrame.PAGE_DASHBOARD));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.add(back);
        root.add(footer, BorderLayout.SOUTH);

        add(root);
    }
}
