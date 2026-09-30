/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

/**
 *
 * @author DELL
 */
package com.mycompany.petclinic.ui;
 
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/*
 * Khung chính: thanh menu trái, vùng nội dung (CardLayout), điều hướng giữa các panel.
 * Không chứa logic nghiệp vụ. Các module khác đăng ký panel qua registerPanel(...).
 */
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
 
    private static final Color SIDEBAR_BG = new Color(15, 43, 92);
    private static final Color SELECTED_BG = new Color(37, 99, 235);
    private static final Color HOVER_BG = new Color(30, 64, 124);
    private static final Font ICON_FONT = new Font("Segoe UI Emoji", Font.PLAIN, 16);
 
    // {key, nhãn, icon}
    private static final String[][] MENU = {
        {KEY_DASHBOARD, "Dashboard", "🏠"},
        {KEY_CUSTOMER, "Khách hàng", "👤"},
        {KEY_PET, "Thú cưng", "🐾"},
        {KEY_DOCTOR, "Bác sĩ", "🧑‍⚕️"},
        {KEY_APPOINTMENT, "Lịch hẹn", "📅"},
        {KEY_MEDICAL, "Khám bệnh", "🩺"},
        {KEY_MEDICINE, "Thuốc", "💊"},
        {KEY_INVOICE, "Hóa đơn", "🧾"},
        {KEY_PAYMENT, "Thanh toán", "💳"}
    };
 
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, MenuButton> menuButtons = new LinkedHashMap<>();
    private final Map<String, JPanel> panels = new HashMap<>();
    private final DashboardPanel dashboardPanel = new DashboardPanel();
 
    public MainFrame() {
        super("PetClinic - Quản lý phòng khám thú y");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 760);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
 
        contentPanel.setBackground(Color.WHITE);
        registerPanel(KEY_WELCOME, createWelcomePanel());
        registerPanel(KEY_DASHBOARD, dashboardPanel);
        dashboardPanel.setOnViewAllAppointments(() -> {
            showPanel(KEY_APPOINTMENT);
        });
 
        add(createSidebar(), BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
 
        // Như thiết kế: mở app hiện màn hình chào, mục Dashboard được tô sáng
        cardLayout.show(contentPanel, KEY_WELCOME);
        selectMenu(KEY_DASHBOARD);
    }
 
    /* API điều hướng*/
    /* Đăng ký (hoặc thay thế) một panel để hiển thị trong vùng nội dung. */
    public final void registerPanel(String key, JPanel panel) {
        JPanel old = panels.put(key, panel);
        if (old != null) {
            contentPanel.remove(old);
        }
        contentPanel.add(panel, key);
        contentPanel.revalidate();
    }
 
    /* Chuyển sang panel theo key; nếu chưa đăng ký sẽ hiện panel "đang phát triển". */
    public void showPanel(String key) {
        if (!panels.containsKey(key)) {
            String title = key;
            for (String[] m : MENU) {
                if (m[0].equals(key)) {
                    title = m[1];
                }
            }
            registerPanel(key, createPlaceholder(title));
        }
        cardLayout.show(contentPanel, key);
        selectMenu(key);
    }
 
    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }
 
    private void selectMenu(String key) {
        menuButtons.forEach((k, b) -> b.setSelected(k.equals(key)));
    }
 
    protected void handleLogout() {
        int r = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn đăng xuất?",
                "Đăng xuất", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            dispose(); // TODO: mở lại màn hình đăng nhập
        }
    }
 
    /*Dựng giao diện*/
    private JPanel createSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(SIDEBAR_BG);
        side.setBorder(new EmptyBorder(16, 12, 16, 12));
        side.setPreferredSize(new Dimension(230, 0));
 
        side.add(createLogo());
        side.add(Box.createVerticalStrut(20));
 
        for (String[] m : MENU) {
            MenuButton b = new MenuButton(m[2], m[1]);
            b.addActionListener(e -> showPanel(m[0]));
            menuButtons.put(m[0], b);
            side.add(b);
            side.add(Box.createVerticalStrut(4));
        }
 
        side.add(Box.createVerticalGlue());
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 90, 140));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        side.add(sep);
        side.add(Box.createVerticalStrut(10));
 
        MenuButton logout = new MenuButton("🚪", "Đăng xuất");
        logout.addActionListener(e -> handleLogout());
        side.add(logout);
        return side;
    }
 
    private JPanel createLogo() {
        JPanel p = new JPanel(new BorderLayout(10, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
 
        JComponent circle = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(59, 130, 246));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String s = "🐾";
                g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2,
                        (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        };
        circle.setPreferredSize(new Dimension(42, 42));
 
        JLabel name = new JLabel("PetClinic");
        name.setFont(new Font("Segoe UI", Font.BOLD, 20));
        name.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Vì sức khỏe thú cưng");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 200, 235));
        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(name);
        text.add(sub);
 
        p.add(circle, BorderLayout.WEST);
        p.add(text, BorderLayout.CENTER);
        return p;
    }
 
    private JPanel createWelcomePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(250, 252, 255));
        JLabel icon = new JLabel("🐶🐱", SwingConstants.CENTER);
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 72));
        JLabel title = new JLabel("Chào mừng đến với PetClinic!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(new Color(15, 43, 92));
        JLabel hint = new JLabel("Vui lòng chọn một chức năng từ menu bên trái để bắt đầu.", SwingConstants.CENTER);
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        hint.setForeground(new Color(80, 90, 110));
 
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets(4, 0, 4, 0);
        p.add(icon, c);
        p.add(title, c);
        p.add(hint, c);
        return p;
    }
 
    private JPanel createPlaceholder(String title) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(245, 247, 250));
        JLabel l = new JLabel("Chức năng \"" + title + "\" đang được phát triển");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        l.setForeground(new Color(100, 110, 130));
        p.add(l);
        return p;
    }
 
    /* Nút menu tự vẽ: nền bo góc khi chọn/hover. */
    private static class MenuButton extends JButton {
        private final String icon;
        private final String label;
        private boolean selected;
        private boolean hover;
 
        MenuButton(String icon, String label) {
            this.icon = icon;
            this.label = label;
            setFont(new Font("Segoe UI", Font.PLAIN, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setPreferredSize(new Dimension(206, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }
 
                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }
 
        @Override
        public void setSelected(boolean b) {
            selected = b;
            repaint();
        }
 
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (selected || hover) {
                g2.setColor(selected ? SELECTED_BG : HOVER_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            }
            g2.setColor(Color.WHITE);
            g2.setFont(ICON_FONT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(icon, 14, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.setFont(getFont());
            fm = g2.getFontMetrics();
            g2.drawString(label, 48, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
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

    /**
     * @param args
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
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
        //</editor-fold>
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MainFrame().setVisible(true));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
