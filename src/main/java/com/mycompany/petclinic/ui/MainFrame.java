package com.mycompany.petclinic.ui;

import com.mycompany.petclinic.ui.customer.CustomerPanel;
import com.mycompany.petclinic.ui.pet.PetPanel;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

public class MainFrame extends JFrame {
    public static final String KEY_WELCOME = "welcome";
    public static final String KEY_DASHBOARD = "dashboard";
    public static final String KEY_CUSTOMER = "customer";
    public static final String KEY_PET = "pet";

    public static final String KEY_DOCTOR = "doctor";
    public static final String KEY_APPOINTMENT = "appointment";
    public static final String KEY_MEDICAL = "medical";
    public static final String KEY_MEDICINE = "medicine";
    public static final String KEY_INVOICE = "invoice";
    public static final String KEY_PAYMENT = "payment";

    // ----- Bảng màu & font (đồng bộ với các panel) -----
    private static final String FONT = "Segoe UI";
    private static final Color BG = new Color(244, 247, 252);
    private static final Color SIDEBAR_BG = new Color(28, 52, 102);     // PRIMARY_DARK
    private static final Color SELECTED_BG = new Color(46, 98, 196);    // PRIMARY
    private static final Color HOVER_BG = new Color(40, 70, 132);
    private static final Color SUB_TEXT = new Color(180, 200, 235);

    // {key, nhãn}
    private static final String[][] MENU = {
        {KEY_DASHBOARD, "Trang chủ"},
        {KEY_CUSTOMER, "Khách hàng"},
        {KEY_PET, "Thú cưng"},
        {KEY_DOCTOR, "Bác sĩ"},
        {KEY_APPOINTMENT, "Lịch hẹn"},
        {KEY_MEDICAL, "Khám bệnh"},
        {KEY_MEDICINE, "Thuốc"},
        {KEY_INVOICE, "Hóa đơn"},
        {KEY_PAYMENT, "Thanh toán"}
    };

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, JButton> menuButtons = new LinkedHashMap<>();
    private final Map<String, JPanel> panels = new HashMap<>();
    private final DashboardPanel dashboardPanel = new DashboardPanel();
    private String selectedKey;

    public MainFrame() {
        super("PetClinic - Quản lý phòng khám thú y");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);

        contentPanel.setBackground(BG);

        registerPanel(KEY_WELCOME, createWelcomePanel());
        registerPanel(KEY_DASHBOARD, dashboardPanel);

        PetPanel petPanel = new PetPanel();

        registerPanel(KEY_CUSTOMER, new CustomerPanel(customerId -> {
            showPanel(KEY_PET);
            petPanel.openForCustomer(customerId);
        }));
        registerPanel(KEY_PET, petPanel);

        dashboardPanel.setOnViewAllAppointments(() -> showPanel(KEY_APPOINTMENT));

        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // Mở chương trình ở trang Dashboard
        showPanel(KEY_DASHBOARD);
    }

    // API điều hướng
    // Đăng ký (hoặc thay thế) một panel để hiển thị trong vùng nội dung.
    public final void registerPanel(String key, JPanel panel) {
        JPanel old = panels.put(key, panel);
        if (old != null) {
            contentPanel.remove(old);
        }
        contentPanel.add(panel, key);
        contentPanel.revalidate();
    }

    // Chuyển sang panel theo key; nếu chưa đăng ký thì báo "đang phát triển".
    public void showPanel(String key) {
        if (!panels.containsKey(key)) {
            JOptionPane.showMessageDialog(this, "Chức năng này đang được phát triển.");
            return;
        }
        cardLayout.show(contentPanel, key);
        selectMenu(key);
        if (KEY_DASHBOARD.equals(key)) {
            dashboardPanel.loadData();   // số liệu luôn mới khi quay về Trang chủ
        }
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    private void selectMenu(String key) {
        selectedKey = key;
        menuButtons.forEach((k, b) -> {
            boolean on = k.equals(key);
            b.setBackground(on ? SELECTED_BG : SIDEBAR_BG);
            b.setFont(new Font(FONT, on ? Font.BOLD : Font.PLAIN, 14));
        });
    }

    protected void handleLogout() {
        int r = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?",
                "Đăng xuất", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            dispose(); // TODO: mở lại màn hình đăng nhập
        }
    }

    // Dựng giao diện
    private JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout(0, 12));
        side.setBackground(SIDEBAR_BG);
        side.setBorder(BorderFactory.createEmptyBorder(18, 12, 16, 12));
        side.setPreferredSize(new Dimension(210, 0));

        // Tiêu đề
        JLabel title = new JLabel("PetClinic");
        title.setFont(new Font(FONT, Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Vì sức khỏe thú cưng");
        sub.setFont(new Font(FONT, Font.PLAIN, 12));
        sub.setForeground(SUB_TEXT);
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(SIDEBAR_BG);
        header.setBorder(BorderFactory.createEmptyBorder(0, 6, 8, 0));
        header.add(title, BorderLayout.NORTH);
        header.add(sub, BorderLayout.SOUTH);
        side.add(header, BorderLayout.NORTH);

        // Các nút menu
        JPanel menu = new JPanel();
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBackground(SIDEBAR_BG);
        for (String[] m : MENU) {
            JButton b = createMenuButton(m[1]);
            b.addActionListener(e -> showPanel(m[0]));
            menuButtons.put(m[0], b);
            menu.add(b);
            menu.add(javax.swing.Box.createVerticalStrut(4));
        }
        side.add(menu, BorderLayout.CENTER);

        // Đăng xuất
        JButton logout = createMenuButton("Đăng xuất");
        logout.addActionListener(e -> handleLogout());
        side.add(logout, BorderLayout.SOUTH);

        return side;
    }

    private JButton createMenuButton(String text) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());          // vẽ phẳng, giống các nút trong panel
        b.setFont(new Font(FONT, Font.PLAIN, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(SIDEBAR_BG);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 14));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setAlignmentX(LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b != menuButtons.get(selectedKey)) {
                    b.setBackground(HOVER_BG);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(b == menuButtons.get(selectedKey) ? SELECTED_BG : SIDEBAR_BG);
            }
        });
        return b;
    }

    private JPanel createWelcomePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        JLabel title = new JLabel("Chào mừng đến với PetClinic!", SwingConstants.CENTER);
        title.setFont(new Font(FONT, Font.BOLD, 26));
        title.setForeground(SIDEBAR_BG);
        p.add(title);
        return p;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("PetClinic - Quản lý phòng khám thú y");
        setPreferredSize(new java.awt.Dimension(1280, 720));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
}