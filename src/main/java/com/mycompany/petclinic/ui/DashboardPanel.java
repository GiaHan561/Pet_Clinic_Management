package com.mycompany.petclinic.ui;

import com.mycompany.petclinic.dao.DashboardDAO;
import com.mycompany.petclinic.dao.PetDAO;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class DashboardPanel extends JPanel {

    // ----- Bảng màu & font (đồng bộ với CustomerPanel) -----
    private static final String FONT = "Segoe UI";
    private static final Color BG = new Color(244, 247, 252);
    private static final Color PRIMARY = new Color(46, 98, 196);
    private static final Color PRIMARY_DARK = new Color(28, 52, 102);
    private static final Color TEXT = new Color(33, 42, 60);
    private static final Color LABEL = new Color(88, 98, 120);
    private static final Color LINE = new Color(205, 212, 226);
    private static final Color READONLY = new Color(237, 240, 246);
    private static final Color ZEBRA = new Color(246, 249, 255);
    private static final Color SELECT = new Color(213, 228, 252);
    private static final Color DISABLED = new Color(208, 213, 223);

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Thời gian", "Khách hàng", "Thú cưng", "Bác sĩ", "Lý do", "Trạng thái"}, 0) {
        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final PetDAO petDAO = new PetDAO();
    private Runnable onViewAllAppointments;
    private Runnable onViewAllNotifications;
    private Runnable onRefresh;

    public DashboardPanel() {
        initComponents();
        initTable();
        applyStyle();

        btnRefresh.addActionListener(e -> {
            loadData();
            if (onRefresh != null) {
                onRefresh.run();
            }
        });
        btnViewAll.addActionListener(e -> {
            if (onViewAllAppointments != null) {
                onViewAllAppointments.run();
            }
        });
        loadData();
    }

    // ------------------------------------------------------------------
    // Bảng
    // ------------------------------------------------------------------
    private void initTable() {
        tblData.setModel(model);
        tblData.setAutoCreateRowSorter(true);
        tblData.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        int[] widths = {150, 190, 130, 190, 240, 130};
        for (int i = 0; i < widths.length; i++) {
            tblData.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    // ------------------------------------------------------------------
    // Nạp dữ liệu
    // ------------------------------------------------------------------
    /* Mỗi thống kê chạy riêng để một truy vấn lỗi không làm hỏng các ô còn lại. */
    public final void loadData() {
        setDate(LocalDate.now());

        try {
            txtCustomer.setText(String.valueOf(dashboardDAO.countCustomers()));
        } catch (SQLException ex) {
            txtCustomer.setText("Lỗi");
            logError("khách hàng", ex);
        }

        try {
            txtPet.setText(String.valueOf(petDAO.countAll()));
        } catch (SQLException ex) {
            txtPet.setText("Lỗi");
            logError("thú cưng", ex);
        }

        try {
            txtToday.setText(String.valueOf(dashboardDAO.countTodayAppointments()));
        } catch (SQLException ex) {
            txtToday.setText("Lỗi");
            logError("lịch hẹn hôm nay", ex);
        }

        try {
            txtRevenue.setText(String.format("%,.0f đ", dashboardDAO.monthRevenue()));
        } catch (SQLException ex) {
            txtRevenue.setText("Lỗi");
            logError("doanh thu", ex);
        }

        try {
            setUpcomingAppointments(dashboardDAO.findRecentAppointments());
        } catch (SQLException ex) {
            model.setRowCount(0);
            logError("lịch hẹn gần đây", ex);
        }
    }

    private void logError(String what, SQLException ex) {
        System.err.println("Dashboard - lỗi tải " + what + ": " + ex.getMessage());
    }

    // API cập nhật dữ liệu
    public void setDate(LocalDate d) {
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        lblDate.setText("Hôm nay: " + days[d.getDayOfWeek().getValue() - 1] + ", "
                + d.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    public void setCustomerStat(String value, String trend) {
        txtCustomer.setText(join(value, trend));
    }

    public void setPetStat(String value, String trend) {
        txtPet.setText(join(value, trend));
    }

    public void setTodayAppointmentStat(String value, String trend) {
        txtToday.setText(join(value, trend));
    }

    public void setRevenueStat(double amount, String trend) {
        txtRevenue.setText(join(String.format("%,.0f đ", amount), trend));
    }

    /* Mỗi dòng: {thời gian, khách hàng, thú cưng, bác sĩ, lý do, trạng thái}. */
    public void setUpcomingAppointments(List<String[]> rows) {
        model.setRowCount(0);
        rows.forEach(model::addRow);
    }

    public void setRevenueChart(String[] labels, double[] values) { }

    public void setNotifications(List<String[]> items) { }

    public void setOnViewAllAppointments(Runnable r) { this.onViewAllAppointments = r; }

    public void setOnViewAllNotifications(Runnable r) { this.onViewAllNotifications = r; }

    /* Nút "Làm mới" sẽ gọi hàm này để nạp lại dữ liệu từ DAO. */
    public void setOnRefresh(Runnable r) { this.onRefresh = r; }

    private String join(String value, String trend) {
        if (value == null) {
            return "";
        }
        return (trend == null || trend.isBlank()) ? value : value + "  (" + trend.trim() + ")";
    }

    // ------------------------------------------------------------------
    // Làm đẹp giao diện (cùng phong cách CustomerPanel)
    // ------------------------------------------------------------------
    private void applyStyle() {
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        pnlTop.setOpaque(false);
        pnlActions.setOpaque(false);

        // Tiêu đề
        lblTitle.setFont(new Font(FONT, Font.BOLD, 26));
        lblTitle.setForeground(PRIMARY_DARK);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));

        // Khung thống kê dạng "thẻ" trắng có tiêu đề
        pnlFields.setBackground(Color.WHITE);
        TitledBorder titled = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                " Thống kê nhanh ",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font(FONT, Font.BOLD, 13), PRIMARY);
        pnlFields.setBorder(BorderFactory.createCompoundBorder(
                titled, BorderFactory.createEmptyBorder(6, 14, 12, 14)));

        for (JLabel l : new JLabel[]{lblCustomer, lblPet, lblToday, lblRevenue}) {
            l.setFont(new Font(FONT, Font.BOLD, 13));
            l.setForeground(LABEL);
        }
        for (JTextField f : new JTextField[]{txtCustomer, txtPet, txtToday, txtRevenue}) {
            f.setFont(new Font(FONT, Font.BOLD, 15));
            f.setForeground(TEXT);
            f.setBackground(READONLY);
            f.setBorder(fieldBorder());
        }

        // Nút bấm
        styleButton(btnRefresh, new Color(26, 148, 168));
        styleButton(btnViewAll, PRIMARY);

        lblDate.setFont(new Font(FONT, Font.ITALIC, 13));
        lblDate.setForeground(LABEL);
        lblDate.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // Bảng
        tblData.setFont(new Font(FONT, Font.PLAIN, 14));
        tblData.setRowHeight(32);
        tblData.setShowVerticalLines(false);
        tblData.setShowHorizontalLines(true);
        tblData.setGridColor(new Color(232, 236, 244));
        tblData.setIntercellSpacing(new Dimension(0, 1));
        tblData.setSelectionBackground(SELECT);
        tblData.setSelectionForeground(TEXT);
        tblData.setFillsViewportHeight(true);
        tblData.setDefaultRenderer(Object.class, new CellRenderer());

        JTableHeader header = tblData.getTableHeader();
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));

        scrollTable.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(LINE, 1, true),
                        " Lịch hẹn gần đây ",
                        TitledBorder.LEFT, TitledBorder.TOP,
                        new Font(FONT, Font.BOLD, 13), PRIMARY),
                BorderFactory.createEmptyBorder(4, 6, 6, 6)));
        scrollTable.setOpaque(false);
        scrollTable.getViewport().setBackground(Color.WHITE);
    }

    private Border fieldBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE, 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10));
    }

    private void styleButton(JButton b, Color base) {
        b.setUI(new BasicButtonUI());
        b.setFont(new Font(FONT, Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setBackground(base);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (b.isEnabled()) {
                    b.setBackground(base.darker());
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.setBackground(b.isEnabled() ? base : DISABLED);
            }
        });
        b.addPropertyChangeListener("enabled",
                e -> b.setBackground(b.isEnabled() ? base : DISABLED));
    }

    /** Vẽ ô dữ liệu: kẻ sọc xen kẽ, căn lề. */
    private class CellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, sel, false, row, col);
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(LEFT);
            setForeground(TEXT);
            setBackground(sel ? SELECT : (row % 2 == 0 ? Color.WHITE : ZEBRA));
            return this;
        }
    }

    /** Vẽ tiêu đề cột: nền xanh đậm, chữ trắng đậm. */
    private class HeaderRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, false, false, row, col);
            setOpaque(true);
            setBackground(PRIMARY_DARK);
            setForeground(Color.WHITE);
            setFont(new Font(FONT, Font.BOLD, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(LEFT);
            return this;
        }
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------
    private void initComponents() {
        pnlTop = new JPanel();
        lblTitle = new JLabel();
        lblDate = new JLabel();
        pnlFields = new JPanel();
        lblCustomer = new JLabel();
        txtCustomer = new JTextField();
        lblPet = new JLabel();
        txtPet = new JTextField();
        lblToday = new JLabel();
        txtToday = new JTextField();
        lblRevenue = new JLabel();
        txtRevenue = new JTextField();
        pnlActions = new JPanel();
        btnRefresh = new JButton();
        btnViewAll = new JButton();
        scrollTable = new JScrollPane();
        tblData = new JTable();

        setLayout(new java.awt.BorderLayout(0, 12));
        pnlTop.setLayout(new java.awt.BorderLayout(0, 12));

        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("TỔNG QUAN");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(2, 4, 16, 12));
        lblCustomer.setText("Tổng khách hàng");
        pnlFields.add(lblCustomer);
        txtCustomer.setEditable(false);
        pnlFields.add(txtCustomer);
        lblPet.setText("Tổng thú cưng");
        pnlFields.add(lblPet);
        txtPet.setEditable(false);
        pnlFields.add(txtPet);
        lblToday.setText("Lịch hẹn hôm nay");
        pnlFields.add(lblToday);
        txtToday.setEditable(false);
        pnlFields.add(txtToday);
        lblRevenue.setText("Doanh thu tháng này");
        pnlFields.add(lblRevenue);
        txtRevenue.setEditable(false);
        pnlFields.add(txtRevenue);
        pnlTop.add(pnlFields, java.awt.BorderLayout.CENTER);

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 0));
        btnRefresh.setText("Làm mới");
        pnlActions.add(btnRefresh);
        btnViewAll.setText("Xem lịch hẹn");
        pnlActions.add(btnViewAll);
        pnlActions.add(lblDate);
        pnlTop.add(pnlActions, java.awt.BorderLayout.SOUTH);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        scrollTable.setViewportView(tblData);
        add(scrollTable, java.awt.BorderLayout.CENTER);
    }

    private JButton btnRefresh;
    private JButton btnViewAll;
    private JLabel lblCustomer;
    private JLabel lblDate;
    private JLabel lblPet;
    private JLabel lblRevenue;
    private JLabel lblTitle;
    private JLabel lblToday;
    private JPanel pnlActions;
    private JPanel pnlFields;
    private JPanel pnlTop;
    private JScrollPane scrollTable;
    private JTable tblData;
    private JTextField txtCustomer;
    private JTextField txtPet;
    private JTextField txtRevenue;
    private JTextField txtToday;
}