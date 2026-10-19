package boundary;

import control.AuthSession;
import control.ClientLookupControl;
import control.ClientLookupResult;
import control.UserRole;

import javax.swing.*;
import java.awt.*;

public class LoginPage extends JPanel {

    private final MainFrame frame;
    private final ClientLookupControl clientControl = new ClientLookupControl();

    // Admin inputs
    private final JTextField adminUserField = new JTextField("admin", 15);
    private final JPasswordField adminPassField = new JPasswordField("admin123", 15);

    // Client input
    private final JTextField clientPhoneField = new JTextField(15);

    public LoginPage(MainFrame frame) {
        this.frame = frame;
        setLayout(new GridBagLayout());
        setBackground(new Color(245, 247, 250));

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 225, 225), 1),
                BorderFactory.createEmptyBorder(28, 36, 28, 36)
        ));
        card.setPreferredSize(new Dimension(460, 420));

        // Header
        JLabel title = new JLabel("ParkWise Portal");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("Select role and authenticate to continue");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sub.setForeground(new Color(110, 110, 110));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(sub);
        card.add(Box.createRigidArea(new Dimension(0, 20)));

        // Tabs for Admin vs Client
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("Customer / Subscriber", buildClientTab());
        tabs.addTab("Administrator", buildAdminTab());

        card.add(tabs);
        add(card);
    }

    private JPanel buildAdminTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(18, 12, 12, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Username:"), gc);
        gc.gridx = 1;
        p.add(adminUserField, gc);

        gc.gridx = 0; gc.gridy = 1;
        p.add(new JLabel("Password:"), gc);
        gc.gridx = 1;
        p.add(adminPassField, gc);

        JButton loginBtn = MainFrame.primaryButton("Sign In as Administrator");
        loginBtn.addActionListener(e -> onAdminLogin());

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        p.add(Box.createRigidArea(new Dimension(0, 10)), gc);

        gc.gridy = 3;
        p.add(loginBtn, gc);

        return p;
    }

    private JPanel buildClientTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createEmptyBorder(18, 12, 12, 12));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 6, 6, 6);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0; gc.gridy = 0;
        p.add(new JLabel("Registered Mobile Phone:"), gc);
        gc.gridx = 1;
        p.add(clientPhoneField, gc);

        JButton loginBtn = MainFrame.primaryButton("Enter Subscriber Portal");
        loginBtn.addActionListener(e -> onClientLogin());

        gc.gridx = 0; gc.gridy = 1; gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        p.add(Box.createRigidArea(new Dimension(0, 10)), gc);

        gc.gridy = 2;
        p.add(loginBtn, gc);

        return p;
    }

    private void onAdminLogin() {
        String u = adminUserField.getText().trim();
        String p = new String(adminPassField.getPassword()).trim();

        if (u.equals("admin") && p.equals("admin123")) {
            AuthSession.login(UserRole.ADMIN, u);
            frame.applyRolePermissions(UserRole.ADMIN);
            frame.showDashboard();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid admin credentials (use admin / admin123).", "Auth Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onClientLogin() {
        String phone = clientPhoneField.getText().trim();
        if (phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter your registered phone number.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ClientLookupResult res = clientControl.lookupByPhone(phone);
        if (!res.success) {
            JOptionPane.showMessageDialog(this, "Phone number not found in system.\nPlease verify or register at gate.", "Not Found", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        AuthSession.login(UserRole.CLIENT, res.phone);
        frame.applyRolePermissions(UserRole.CLIENT);
        frame.openCustomerPortal(res.phone);
    }
}