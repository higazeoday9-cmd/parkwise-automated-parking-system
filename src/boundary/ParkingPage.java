package boundary;

import control.ParkingLotControl;
import entity.ParkingLot;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ParkingPage extends JPanel {

    private final ParkingLotControl lotControl = new ParkingLotControl();
    private final DefaultTableModel model;

    public ParkingPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = MainFrame.centerBox();
        header.add(MainFrame.title("Parking Lots"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("View parking lots stored in the database"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        model = new DefaultTableModel(
                new Object[]{"LotNumber", "Address", "IsAvailable"},
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
        refresh.addActionListener(e -> loadLots(refresh));

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

        loadLots(refresh);
    }

    private void loadLots(JButton refreshBtn) {
        refreshBtn.setEnabled(false);
        model.setRowCount(0);

        SwingWorker<List<ParkingLot>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ParkingLot> doInBackground() throws Exception {
                return lotControl.getAllLots();
            }

            @Override
            protected void done() {
                refreshBtn.setEnabled(true);
                try {
                    List<ParkingLot> lots = get();
                    for (ParkingLot p : lots) {
                        model.addRow(new Object[]{
                                p.getLotNumber(),
                                p.getAdress(),       // keep your entity naming
                                p.isAvilaible()      // keep your entity naming
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(ParkingPage.this,
                            "Failed to load parking lots:\n" + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
}
