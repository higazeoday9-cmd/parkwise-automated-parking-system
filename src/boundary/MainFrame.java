package boundary;

import control.AuthSession;
import control.UserRole;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {

    public static final String PAGE_LOGIN           = "login";
    public static final String PAGE_WELCOME         = "welcome";
    public static final String PAGE_DASHBOARD       = "dashboard";
    public static final String PAGE_CONVEYOR        = "conveyor";
    public static final String PAGE_CC              = "conveyorController";
    public static final String PAGE_REPORT          = "report";
    public static final String PAGE_PARKING         = "parking";
    public static final String PAGE_JSON            = "json";
    public static final String PAGE_PRICING         = "pricing";
    public static final String PAGE_GATE            = "gate";
    public static final String PAGE_REGISTER        = "registerVehicle";
    public static final String PAGE_CLIENT_LOOKUP   = "clientLookup";
    public static final String PAGE_SESSIONS        = "parkingSessions";
    public static final String PAGE_CUSTOMER_CLUB   = "customerClub";
    public static final String PAGE_CUSTOMER_PORTAL = "customerPortal";

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cards = new JPanel(cardLayout);

    private final JLabel headerTitle = new JLabel("ParkWise");
    private final JLabel headerSub   = new JLabel("Control Center");

    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private final JPanel sidebar;
    private final JPanel topBar;

    private final VehicleRegistrationPage vehicleRegistrationPage;
    private final CustomerPortalPage customerPortalPage;

    public MainFrame() {
        super("ParkWise - Lots & Conveyor System");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setLocationRelativeTo(null);

        // Register pages
        cards.add(new LoginPage(this), PAGE_LOGIN);
        cards.add(new WelcomePage(this), PAGE_WELCOME);
        cards.add(new DashboardPage(this), PAGE_DASHBOARD);
        cards.add(new ConveyorPage(this), PAGE_CONVEYOR);
        cards.add(new ConveyorControllerPage(this), PAGE_CC);
        cards.add(new ReportPage(this), PAGE_REPORT);
        cards.add(new ParkingPage(this), PAGE_PARKING);
        cards.add(new JsonImportPage(this), PAGE_JSON);
        cards.add(new PricingPage(this), PAGE_PRICING);
        cards.add(new ParkingSessionPage(this), PAGE_SESSIONS);
        cards.add(new CustomerClubPage(this), PAGE_CUSTOMER_CLUB);
        cards.add(new GateSimulatorPage(this), PAGE_GATE);

        vehicleRegistrationPage = new VehicleRegistrationPage(this);
        cards.add(vehicleRegistrationPage, PAGE_REGISTER);

        cards.add(new ClientLookupPage(this), PAGE_CLIENT_LOOKUP);

        customerPortalPage = new CustomerPortalPage(this);
        cards.add(customerPortalPage, PAGE_CUSTOMER_PORTAL);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);

        sidebar = buildSidebar();
        topBar = buildTopBar();

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Color.WHITE);
        main.add(topBar, BorderLayout.NORTH);
        main.add(cards, BorderLayout.CENTER);

        root.add(sidebar, BorderLayout.WEST);
        root.add(main, BorderLayout.CENTER);

        setContentPane(root);

        // Start directly with the clean login screen
        showPage(PAGE_LOGIN);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(240, 0));
        side.setLayout(new BorderLayout());
        side.setBackground(new Color(245, 247, 250));
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(225, 225, 225)));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(new EmptyBorder(18, 16, 12, 16));

        JLabel name = new JLabel("ParkWise");
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        JLabel tag = new JLabel("Control Center");
        tag.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tag.setForeground(new Color(90, 90, 90));

        brand.add(name);
        brand.add(Box.createRigidArea(new Dimension(0, 4)));
        brand.add(tag);

        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(new EmptyBorder(10, 12, 12, 12));

        // Manager / Admin options
        addNav(nav, "Welcome", PAGE_WELCOME);
        addNav(nav, "Dashboard", PAGE_DASHBOARD);
        addNav(nav, "Conveyor Control", PAGE_CONVEYOR);
        addNav(nav, "Conveyor Controller (CC)", PAGE_CC);
        addNav(nav, "Gate Simulator", PAGE_GATE);
        addNav(nav, "Client Lookup", PAGE_CLIENT_LOOKUP);
        addNav(nav, "Parking Sessions", PAGE_SESSIONS);
        addNav(nav, "Customer Club", PAGE_CUSTOMER_CLUB);
        addNav(nav, "Parking Lots", PAGE_PARKING);
        addNav(nav, "IRS JSON Import", PAGE_JSON);
        addNav(nav, "Pricing Rates", PAGE_PRICING);
        addNav(nav, "Generate Report", PAGE_REPORT);

        // Customer navigation button
        addNav(nav, "My Parking Session", PAGE_CUSTOMER_PORTAL);

        // Sign Out Button
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(10, 12, 16, 12));

        JButton logoutBtn = navButton("Sign Out");
        logoutBtn.setForeground(new Color(180, 40, 40));
        logoutBtn.addActionListener(e -> {
            AuthSession.logout();
            showPage(PAGE_LOGIN);
        });
        footer.add(logoutBtn, BorderLayout.NORTH);

        JLabel small = new JLabel("© ParkWise");
        small.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        small.setForeground(new Color(120, 120, 120));
        small.setBorder(new EmptyBorder(8, 4, 0, 0));
        footer.add(small, BorderLayout.SOUTH);

        side.add(brand, BorderLayout.NORTH);

        JScrollPane scrollNav = new JScrollPane(nav);
        scrollNav.setBorder(null);
        scrollNav.setOpaque(false);
        scrollNav.getViewport().setOpaque(false);
        side.add(scrollNav, BorderLayout.CENTER);

        side.add(footer, BorderLayout.SOUTH);

        return side;
    }

    private JPanel buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Color.WHITE);
        top.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        top.setPreferredSize(new Dimension(0, 68));

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(10, 18, 10, 18));

        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        headerSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        headerSub.setForeground(new Color(90, 90, 90));

        left.add(headerTitle);
        left.add(Box.createRigidArea(new Dimension(0, 3)));
        left.add(headerSub);

        top.add(left, BorderLayout.WEST);
        return top;
    }

    private void addNav(JPanel nav, String label, String pageKey) {
        JButton b = navButton(label);
        b.addActionListener(e -> showPage(pageKey));
        navButtons.put(pageKey, b);
        nav.add(b);
        nav.add(Box.createRigidArea(new Dimension(0, 6)));
    }

    private JButton navButton(String text) {
        JButton b = new JButton(text);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(245, 247, 250));
        b.setForeground(new Color(35, 35, 35));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        return b;
    }

    public void showPage(String key) {
        // Hide sidebar and topbar on the Login page
        boolean isLoginPage = PAGE_LOGIN.equals(key);
        sidebar.setVisible(!isLoginPage);
        topBar.setVisible(!isLoginPage);

        if (!isLoginPage) {
            switch (key) {
                case PAGE_WELCOME -> { headerTitle.setText("Welcome"); headerSub.setText("ParkWise Administrator Portal"); }
                case PAGE_DASHBOARD -> { headerTitle.setText("Dashboard"); headerSub.setText("System Management Modules"); }
                case PAGE_CONVEYOR -> { headerTitle.setText("Conveyor Control"); headerSub.setText("Mechanical & Electronic Diagnostics"); }
                case PAGE_CC -> { headerTitle.setText("Conveyor Controller (CC)"); headerSub.setText("Vehicle-to-Space Spatial Optimization"); }
                case PAGE_CLIENT_LOOKUP -> { headerTitle.setText("Client Lookup"); headerSub.setText("Inspect Active Client Sessions"); }
                case PAGE_REPORT -> { headerTitle.setText("Generate Report"); headerSub.setText("JasperReports Annual Financial Summary (PDF)"); }
                case PAGE_PARKING -> { headerTitle.setText("Parking Lots"); headerSub.setText("Facility Capacity & Location Directory"); }
                case PAGE_JSON -> { headerTitle.setText("IRS JSON Import"); headerSub.setText("Annual Tariff Rate Matrix Ingestion"); }
                case PAGE_PRICING -> { headerTitle.setText("Pricing Rates"); headerSub.setText("Active Lot Rate History"); }
                case PAGE_GATE -> { headerTitle.setText("Gate Sensor Simulator"); headerSub.setText("Ingress License Scanning"); }
                case PAGE_REGISTER -> { headerTitle.setText("Vehicle Registration"); headerSub.setText("Client Onboarding & Barrier Fallback"); }
                case PAGE_SESSIONS -> { headerTitle.setText("Parking Sessions"); headerSub.setText("Real-time Lot Session Directory"); }
                case PAGE_CUSTOMER_CLUB -> { headerTitle.setText("Customer Club"); headerSub.setText("Membership & Preferred Facilities"); }
                case PAGE_CUSTOMER_PORTAL -> { headerTitle.setText("Subscriber Portal"); headerSub.setText("Self-Service Parking & Exit Checkout"); }
                default -> { headerTitle.setText("ParkWise"); headerSub.setText("Control Center"); }
            }

            for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
                boolean active = entry.getKey().equals(key);
                entry.getValue().setBackground(active ? new Color(225, 238, 255) : new Color(245, 247, 250));
            }
        }

        cardLayout.show(cards, key);
    }

    public void applyRolePermissions(UserRole role) {
        boolean isAdmin = (role == UserRole.ADMIN);

        // Admin exclusive views
        setNavVisible(PAGE_WELCOME, isAdmin);
        setNavVisible(PAGE_DASHBOARD, isAdmin);
        setNavVisible(PAGE_CONVEYOR, isAdmin);
        setNavVisible(PAGE_CC, isAdmin);
        setNavVisible(PAGE_GATE, isAdmin);
        setNavVisible(PAGE_CLIENT_LOOKUP, isAdmin);
        setNavVisible(PAGE_SESSIONS, isAdmin);
        setNavVisible(PAGE_CUSTOMER_CLUB, isAdmin);
        setNavVisible(PAGE_PARKING, isAdmin);
        setNavVisible(PAGE_JSON, isAdmin);
        setNavVisible(PAGE_PRICING, isAdmin);
        setNavVisible(PAGE_REPORT, isAdmin);

        // Customer exclusive views
        setNavVisible(PAGE_CUSTOMER_PORTAL, !isAdmin);

        headerSub.setText("Signed in: " + role.getDisplayName() +
                (AuthSession.getCurrentUserIdentifier() != null ? " (" + AuthSession.getCurrentUserIdentifier() + ")" : ""));
    }

    private void setNavVisible(String key, boolean visible) {
        if (navButtons.containsKey(key)) {
            navButtons.get(key).setVisible(visible);
        }
    }

    public void showDashboard() { showPage(PAGE_DASHBOARD); }

    public void openCustomerPortal(String phone) {
        customerPortalPage.loadCustomer(phone);
        showPage(PAGE_CUSTOMER_PORTAL);
    }

    public void openVehicleRegistration(int lotNumber, String scannedVehicleNumber) {
        vehicleRegistrationPage.setContext(lotNumber, scannedVehicleNumber);
        showPage(PAGE_REGISTER);
    }

    public void openExitParking(String vehicleNumber, String phone) {
        openCustomerPortal(phone);
    }

    // ---------- styling helpers ----------
    public static JLabel title(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 26));
        return l;
    }

    public static JLabel subtitle(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(new Color(90, 90, 90));
        return l;
    }

    public static JSeparator niceLine() {
        JSeparator s = new JSeparator();
        s.setForeground(new Color(225, 225, 225));
        return s;
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(30, 120, 230));
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBackground(new Color(245, 245, 245));
        b.setForeground(new Color(30, 30, 30));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return b;
    }

    public static JPanel pageContainer() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new EmptyBorder(24, 28, 24, 28));
        p.setBackground(Color.WHITE);
        return p;
    }

    public static JPanel centerBox() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    public static void space(JPanel p, int px) {
        p.add(Box.createRigidArea(new Dimension(0, px)));
    }
}