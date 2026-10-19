package boundary;

import javax.swing.*;
import java.awt.*;

public class DashboardPage extends JPanel {

    public DashboardPage(MainFrame frame) {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.WHITE);

        JPanel root = MainFrame.pageContainer();

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(MainFrame.title("Dashboard"));
        MainFrame.space(header, 6);
        header.add(MainFrame.subtitle("Choose a module below"));
        MainFrame.space(header, 12);
        header.add(MainFrame.niceLine());
        root.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(3, 3, 16, 16));
        grid.setOpaque(false);

        JButton report = MainFrame.secondaryButton("Generate Report");
        JButton parking = MainFrame.secondaryButton("Parking Lots");
        JButton conveyors = MainFrame.secondaryButton("Conveyor Control");

        JButton cc = MainFrame.secondaryButton("Conveyor Controller (CC)");
        JButton json = MainFrame.secondaryButton("IRS JSON Import");
        JButton pricing = MainFrame.secondaryButton("Pricing Rates");

        JButton gate = MainFrame.secondaryButton("Gate Simulator (Arrival)");
        JButton lookup = MainFrame.secondaryButton("Client Lookup (Phone)");
        JButton club = MainFrame.secondaryButton("Customer Club");

        report.addActionListener(e -> frame.showPage(MainFrame.PAGE_REPORT));
        parking.addActionListener(e -> frame.showPage(MainFrame.PAGE_PARKING));
        conveyors.addActionListener(e -> frame.showPage(MainFrame.PAGE_CONVEYOR));

        cc.addActionListener(e -> frame.showPage(MainFrame.PAGE_CC));
        json.addActionListener(e -> frame.showPage(MainFrame.PAGE_JSON));
        pricing.addActionListener(e -> frame.showPage(MainFrame.PAGE_PRICING));

        gate.addActionListener(e -> frame.showPage(MainFrame.PAGE_GATE));
        lookup.addActionListener(e -> frame.showPage(MainFrame.PAGE_CLIENT_LOOKUP));
        club.addActionListener(e -> frame.showPage(MainFrame.PAGE_CUSTOMER_CLUB));

        grid.add(wrapCard("Annual Summary PDF", "Generate yearly totals, income, and averages.", report));
        grid.add(wrapCard("Parking Lots", "View and manage parking lot data.", parking));
        grid.add(wrapCard("Conveyors", "Turn on/off, pause/resume, weight settings.", conveyors));

        grid.add(wrapCard("Conveyor Controller", "Assign conveyor + nearest compatible space.", cc));
        grid.add(wrapCard("IRS Import", "Import pricing JSON into DB.", json));
        grid.add(wrapCard("Rates Table", "View current price rates stored in DB.", pricing));

        grid.add(wrapCard("Gate Arrival", "Simulate GS arrival + registration flow.", gate));
        grid.add(wrapCard("Client Lookup", "Client views parking details by phone.", lookup));
        grid.add(wrapCard("Customer Club", "Manage club members, preferred lots, and growth reports.", club));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(22, 0, 0, 0));
        center.add(grid, BorderLayout.NORTH);

        root.add(center, BorderLayout.CENTER);

        JButton back = MainFrame.secondaryButton("Back to Welcome");
        back.addActionListener(e -> frame.showPage(MainFrame.PAGE_WELCOME));
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        footer.add(back);
        root.add(footer, BorderLayout.SOUTH);

        content.add(root, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setOpaque(false);

        add(scroll, BorderLayout.CENTER);
    }

    private JPanel wrapCard(String title, String desc, JButton action) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(250, 250, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JLabel d = new JLabel("<html><div style='width:260px;'>" + desc + "</div></html>");
        d.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        d.setForeground(new Color(90, 90, 90));

        card.add(t);
        card.add(Box.createRigidArea(new Dimension(0, 8)));
        card.add(d);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
        card.add(action);

        return card;
    }
}



