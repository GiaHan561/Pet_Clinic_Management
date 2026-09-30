/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author DELL
 */
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */

/**
 *
 * @author DELL
 */
package com.mycompany.petclinic.ui;
 
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class DashboardPanel extends javax.swing.JPanel {
    /**
     * Creates new form DashboardPanel
     */
    
    /*
    * Màn hình tổng quan: thẻ thống kê nhanh, lịch hẹn sắp tới, biểu đồ doanh thu, thông báo.
    * Chỉ hiển thị (không có logic nghiệp vụ). Dữ liệu được đẩy vào qua các hàm set...
    * Dữ liệu mẫu trong loadSampleData() chỉ để xem giao diện.
    */
 
    private static final Color BG = new Color(245, 247, 250);
    private static final Color BORDER = new Color(229, 233, 240);
    private static final Color TEXT = new Color(30, 41, 59);
    private static final Color MUTED = new Color(120, 130, 150);
    private static final Color BLUE = new Color(37, 99, 235);
    private static final Color GREEN = new Color(22, 163, 74);
 
    private final StatCard customerCard = new StatCard("Khách hàng", "👥", new Color(232, 242, 255), new Color(37, 99, 235));
    private final StatCard petCard = new StatCard("Thú cưng", "🐾", new Color(228, 246, 236), new Color(22, 163, 74));
    private final StatCard todayCard = new StatCard("Lịch hẹn hôm nay", "📅", new Color(241, 237, 255), new Color(109, 40, 217));
    private final StatCard revenueCard = new StatCard("Doanh thu tháng này", "💰", new Color(255, 244, 228), new Color(217, 119, 6));
 
    private final JLabel dateLabel = new JLabel();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Thời gian", "Khách hàng", "Thú cưng", "Dịch vụ", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final RevenueChart chart = new RevenueChart();
    private final JPanel notificationList = new JPanel();
    private Runnable onViewAllAppointments;
    private Runnable onViewAllNotifications;
 
    public DashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));
 
        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        // Dashboard chỉ dựng giao diện.
        // Dữ liệu thật sẽ được truyền vào qua các hàm set...()
        // sau này khi DAO/Service của nhóm hoàn thành.
        setDate(LocalDate.now());
    }
 
    /*API cập nhật dữ liệu*/
    public void setDate(LocalDate d) {
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        dateLabel.setText("📅  " + days[d.getDayOfWeek().getValue() - 1] + ", "
                + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }
 
    public void setCustomerStat(String value, String trend) { customerCard.set(value, trend); }
    public void setPetStat(String value, String trend) { petCard.set(value, trend); }
    public void setTodayAppointmentStat(String value, String trend) { todayCard.set(value, trend); }
    public void setRevenueStat(double amount, String trend) { revenueCard.set(formatMoney(amount), trend); }
 
    /* Mỗi dòng: {thời gian, khách hàng, thú cưng, dịch vụ, trạng thái}. */
    public void setUpcomingAppointments(List<String[]> rows) {
        tableModel.setRowCount(0);
        rows.forEach(tableModel::addRow);
    }
 
    public void setRevenueChart(String[] labels, double[] values) {
        chart.setData(labels, values);
    }
 
    /* Mỗi dòng: {loại (error|warning|success), nội dung, thời gian}. */
    public void setNotifications(List<String[]> items) {
        notificationList.removeAll();
        for (String[] n : items) {
            notificationList.add(notificationRow(n[0], n[1], n[2]));
        }
        notificationList.revalidate();
        notificationList.repaint();
    }
 
    public void setOnViewAllAppointments(Runnable r) { this.onViewAllAppointments = r; }
    public void setOnViewAllNotifications(Runnable r) { this.onViewAllNotifications = r; }
 
    /*Dựng giao diện*/
    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel title = new JLabel("📊  Tổng quan");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateLabel.setForeground(MUTED);
        p.add(title, BorderLayout.WEST);
        p.add(dateLabel, BorderLayout.EAST);
        return p;
    }
 
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setOpaque(false);
 
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.setPreferredSize(new Dimension(0, 104));
        stats.add(customerCard);
        stats.add(petCard);
        stats.add(todayCard);
        stats.add(revenueCard);
        body.add(stats, BorderLayout.NORTH);
 
        JPanel bottom = new JPanel(new GridBagLayout());
        bottom.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.gridx = 0;
        c.weightx = 0.56;
        c.insets = new Insets(0, 0, 0, 8);
        bottom.add(buildAppointmentCard(), c);
 
        JPanel right = new JPanel(new GridLayout(2, 1, 0, 16));
        right.setOpaque(false);
        right.add(buildChartCard());
        right.add(buildNotificationCard());
        c.gridx = 1;
        c.weightx = 0.44;
        c.insets = new Insets(0, 8, 0, 0);
        bottom.add(right, c);
 
        body.add(bottom, BorderLayout.CENTER);
        return body;
    }
 
    private JPanel buildAppointmentCard() {
        JTable table = new JTable(tableModel) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                if (getRowCount() == 0) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                    String message = "Chưa có lịch hẹn";
                    g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    g2.setColor(MUTED);

                    FontMetrics fm = g2.getFontMetrics();
                    int x = (getWidth() - fm.stringWidth(message)) / 2;
                    int y = getHeight() / 2 + fm.getAscent() / 2;

                    g2.drawString(message, Math.max(0, x), y);
                    g2.dispose();
                }
            }
        };
        table.setRowHeight(36);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(240, 243, 247));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.setForeground(TEXT);
        table.setSelectionBackground(new Color(239, 246, 255));
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.setFocusable(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int col) {
                super.getTableCellRendererComponent(t, v, sel, false, r, col);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });
        table.getColumnModel().getColumn(4).setCellRenderer(new PillRenderer());
 
        JTableHeader_style(table);
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(null);
        sp.getViewport().setBackground(Color.WHITE);
 
        return card("📅  Lịch hẹn sắp tới", "Xem tất cả",
                () -> { if (onViewAllAppointments != null) onViewAllAppointments.run(); }, sp);
    }
 
    private void JTableHeader_style(JTable table) {
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 30));
        table.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int col) {
                super.getTableCellRendererComponent(t, v, false, false, r, col);
                setBackground(Color.WHITE);
                setForeground(MUTED);
                setFont(new Font("Segoe UI", Font.PLAIN, 11));
                setHorizontalAlignment(LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, 8, 0, 8)));
                return this;
            }
        });
    }
 
    private JPanel buildChartCard() {
        JLabel period = new JLabel("7 ngày qua  ▾");
        period.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        period.setForeground(MUTED);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(chart, BorderLayout.CENTER);
        return card("📈  Thống kê doanh thu", period, wrap);
    }
 
    private JPanel buildNotificationCard() {
        notificationList.setLayout(new BoxLayout(notificationList, BoxLayout.Y_AXIS));
        notificationList.setOpaque(false);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(notificationList, BorderLayout.NORTH);
        return card("🔔  Thông báo", "Xem tất cả",
                () -> { if (onViewAllNotifications != null) onViewAllNotifications.run(); }, wrap);
    }
 
    private JPanel notificationRow(String type, String text, String time) {
        Color dot = "error".equals(type) ? new Color(239, 68, 68)
                : "warning".equals(type) ? new Color(245, 158, 11) : new Color(34, 197, 94);
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dot);
                g2.fillOval(2, 4, 12, 12);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(20, 20));
        JLabel t = new JLabel(text);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        t.setForeground(TEXT);
        JLabel tm = new JLabel(time);
        tm.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tm.setForeground(MUTED);
 
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(3, 0, 3, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        row.add(icon, BorderLayout.WEST);
        row.add(t, BorderLayout.CENTER);
        row.add(tm, BorderLayout.EAST);
        return row;
    }
 
    private JPanel card(String title, String linkText, Runnable onClick, JComponent body) {
        JLabel link = new JLabel(linkText);
        link.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        link.setForeground(BLUE);
        link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        link.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }
        });
        return card(title, link, body);
    }
 
    private JPanel card(String title, JComponent headerRight, JComponent body) {
        RoundedPanel p = new RoundedPanel(Color.WHITE, BORDER, 14);
        p.setLayout(new BorderLayout(0, 6));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setForeground(TEXT);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.add(t, BorderLayout.WEST);
        head.add(headerRight, BorderLayout.EAST);
        p.add(head, BorderLayout.NORTH);
        p.add(body, BorderLayout.CENTER);
        return p;
    }
 
    private static String formatMoney(double v) {
        DecimalFormatSymbols s = new DecimalFormatSymbols();
        s.setGroupingSeparator('.');
        return new DecimalFormat("#,##0", s).format(v) + " đ";
    }
 
    /*
     * Dashboard không chứa dữ liệu mẫu và không truy vấn database.
     *
     * Khi DAO/Service của nhóm hoàn thành, chỉ cần truyền dữ liệu vào
     * qua các API public:
     *
     *   setCustomerStat(...)
     *   setPetStat(...)
     *   setTodayAppointmentStat(...)
     *   setRevenueStat(...)
     *   setUpcomingAppointments(...)
     *   setRevenueChart(...)
     *   setNotifications(...)
     *
     * Nhờ vậy phần giao diện Dashboard có thể hoàn thành trước,
     * còn phần kết nối database được bổ sung sau.
     */

    /*Component phụ*/
    /* Panel nền bo góc, có viền. */
    private static class RoundedPanel extends JPanel {
        private final Color fill;
        private final Color border;
        private final int arc;
 
        RoundedPanel(Color fill, Color border, int arc) {
            this.fill = fill;
            this.border = border;
            this.arc = arc;
            setOpaque(false);
        }
 
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            g2.dispose();
        }
    }
 
    /* Thẻ thống kê: icon tròn, tiêu đề, số lớn, xu hướng. */
    private static class StatCard extends RoundedPanel {
        private final JLabel value = new JLabel("-");
        private final JLabel trend = new JLabel(" ");
 
        StatCard(String title, String emoji, Color bg, Color accent) {
            super(bg, new Color(bg.getRed() - 12, bg.getGreen() - 10, bg.getBlue() - 6), 14);
            setLayout(new BorderLayout(12, 0));
            setBorder(new EmptyBorder(12, 14, 12, 14));
 
            JLabel icon = new JLabel(emoji, SwingConstants.CENTER);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            icon.setPreferredSize(new Dimension(44, 44));
            JPanel iconWrap = new JPanel(new GridBagLayout());
            iconWrap.setOpaque(false);
            iconWrap.add(icon);
 
            JLabel t = new JLabel(title);
            t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            t.setForeground(accent);
            value.setFont(new Font("Segoe UI", Font.BOLD, 22));
            value.setForeground(TEXT);
            trend.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            trend.setForeground(GREEN);
 
            JPanel text = new JPanel(new GridLayout(3, 1));
            text.setOpaque(false);
            text.add(t);
            text.add(value);
            text.add(trend);
 
            add(iconWrap, BorderLayout.WEST);
            add(text, BorderLayout.CENTER);
        }
 
        void set(String v, String tr) {
            value.setText(v);
            trend.setText(tr);
        }
    }
 
    /* Biểu đồ cột doanh thu vẽ bằng Java2D. */
    private static class RevenueChart extends JComponent {
        private String[] labels = new String[0];
        private double[] values = new double[0];
 
        void setData(String[] labels, double[] values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }
 
        @Override
        protected void paintComponent(Graphics g) {
            if (values.length == 0) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            FontMetrics fm = g2.getFontMetrics();
 
            int left = 36, right = 4, top = 6, bottom = 20;
            int w = getWidth() - left - right;
            int h = getHeight() - top - bottom;
 
            double max = 0;
            for (double v : values) {
                max = Math.max(max, v);
            }
            double niceMax = Math.max(10e6, Math.ceil(max / 10e6) * 10e6);
 
            for (int i = 0; i <= 4; i++) {
                int y = top + h - (int) (h * i / 4.0);
                g2.setColor(new Color(238, 241, 246));
                g2.drawLine(left, y, left + w, y);
                double m = niceMax * i / 4 / 1e6;
                String s = (m == Math.floor(m) ? String.valueOf((int) m) : String.format("%.1f", m)) + (i == 0 ? "" : "M");
                g2.setColor(MUTED);
                g2.drawString(s, left - 6 - fm.stringWidth(s), y + fm.getAscent() / 2 - 1);
            }
 
            double slot = (double) w / values.length;
            int barW = (int) (slot * 0.55);
            for (int i = 0; i < values.length; i++) {
                int bh = (int) (h * values[i] / niceMax);
                int x = left + (int) (slot * i + (slot - barW) / 2);
                g2.setColor(new Color(96, 165, 250));
                g2.fillRoundRect(x, top + h - bh, barW, bh, 6, 6);
                g2.fillRect(x, top + h - Math.min(bh, 6), barW, Math.min(bh, 6));
                if (i < labels.length) {
                    g2.setColor(MUTED);
                    g2.drawString(labels[i], x + (barW - fm.stringWidth(labels[i])) / 2, getHeight() - 5);
                }
            }
            g2.dispose();
        }
    }
 
    /* Nhãn trạng thái dạng "viên thuốc". */
    private static class PillLabel extends JLabel {
        private Color bg = Color.LIGHT_GRAY;
 
        PillLabel() {
            setOpaque(false);
            setFont(new Font("Segoe UI", Font.PLAIN, 11));
            setBorder(new EmptyBorder(3, 10, 3, 10));
        }
 
        void setStatus(String s) {
            setText(s);
            if ("Đã xác nhận".equals(s)) {
                bg = new Color(220, 247, 232);
                setForeground(new Color(22, 128, 74));
            } else if ("Chờ xác nhận".equals(s)) {
                bg = new Color(255, 240, 218);
                setForeground(new Color(200, 110, 5));
            } else {
                bg = new Color(235, 238, 243);
                setForeground(MUTED);
            }
        }
 
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }
 
    private static class PillRenderer implements TableCellRenderer {
        private final JPanel holder = new JPanel(new GridBagLayout());
        private final PillLabel pill = new PillLabel();
 
        PillRenderer() {
            GridBagConstraints c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 4, 0, 0);
            holder.add(pill, c);
        }
 
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int col) {
            pill.setStatus(v == null ? "" : v.toString());
            holder.setBackground(sel ? t.getSelectionBackground() : Color.WHITE);
            return holder;
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
