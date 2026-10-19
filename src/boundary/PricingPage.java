package boundary;

import control.PricingControl;
import entity.PriceRate;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PricingPage extends JPanel {

    private final PricingControl pricingControl = new PricingControl();
    private final DefaultTableModel model;

    public PricingPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Pricing Rates"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Rates stored in the database (imported from IRS JSON)"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"PriceListID", "LotNumber", "Year", "FirstHour", "AdditionalHour", "FullDay"},
                0
        ) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane sp = new JScrollPane(table);

        JButton refresh = MainFrame.primaryButton("Refresh");
        refresh.addActionListener(e -> loadRates(refresh));

        JPanel topActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topActions.setOpaque(false);
        topActions.setBorder(BorderFactory.createEmptyBorder(14, 0, 10, 0));
        topActions.add(refresh);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(topActions, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        root.add(center, BorderLayout.CENTER);

        JButton back = MainFrame.secondaryButton("Back");
        back.addActionListener(e -> frame.showPage(MainFrame.PAGE_DASHBOARD));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.add(back);
        root.add(footer, BorderLayout.SOUTH);

        add(root);

        // Auto load first time
        loadRates(refresh);
    }

    private void loadRates(JButton refreshBtn) {
        refreshBtn.setEnabled(false);
        model.setRowCount(0);

        SwingWorker<List<PriceRate>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PriceRate> doInBackground() throws Exception {
                return pricingControl.getAllPriceRates();
            }

            @Override
            protected void done() {
                refreshBtn.setEnabled(true);
                try {
                    List<PriceRate> rates = get();
                    for (PriceRate r : rates) {
                        model.addRow(new Object[]{
                                r.getPriceListID(),
                                r.getLotNumber(),
                                r.getEffectiveYear(),
                                r.getPriceFirstHour(),
                                r.getPriceAdditionalHour(),
                                r.getPriceFullDay()
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PricingPage.this,
                            "Failed to load rates:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}

