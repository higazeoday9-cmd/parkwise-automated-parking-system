package boundary;

import javax.swing.*;
import java.awt.*;

public class WelcomePage extends JPanel {

    public WelcomePage(MainFrame frame) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);

        JPanel box = MainFrame.centerBox();
        box.setMaximumSize(new Dimension(600, 400));

        JLabel t = MainFrame.title("Welcome to ParkWise");
        JLabel t2 = MainFrame.subtitle("Parking Lots & Conveyor Control System");
        JSeparator line = MainFrame.niceLine();

        JButton enter = MainFrame.primaryButton("Enter System");
        enter.addActionListener(e -> frame.showPage(MainFrame.PAGE_DASHBOARD));

        box.add(t);
        MainFrame.space(box, 6);
        box.add(t2);
        MainFrame.space(box, 14);
        box.add(line);
        MainFrame.space(box, 18);

        JLabel msg = MainFrame.subtitle("Manage conveyors, parking lots, pricing rates, JSON imports, and generate annual reports.");
        msg.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        box.add(msg);

        MainFrame.space(box, 24);
        box.add(enter);

        add(box);
    }
}
