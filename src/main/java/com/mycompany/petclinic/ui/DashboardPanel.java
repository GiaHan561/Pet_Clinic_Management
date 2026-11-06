package com.mycompany.petclinic.ui;

import com.mycompany.petclinic.dao.DashboardDAO;
import com.mycompany.petclinic.dao.PetDAO;
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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public class DashboardPanel extends JPanel {
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
        tblData.setModel(model);
        tblData.setRowHeight(27);
        tblData.setAutoCreateRowSorter(true);

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

    /* Nạp số liệu từ CSDL. Mỗi thống kê chạy riêng để một truy vấn lỗi không làm hỏng các ô còn lại. */
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

    // Dựng giao diện 
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

        setLayout(new java.awt.BorderLayout(8, 8));
        pnlTop.setLayout(new java.awt.BorderLayout(8, 8));

        lblTitle.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 21));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setText("TỔNG QUAN");
        pnlTop.add(lblTitle, java.awt.BorderLayout.NORTH);

        pnlFields.setLayout(new java.awt.GridLayout(2, 4, 8, 8));
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

        pnlActions.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 8));
        btnRefresh.setText("Làm mới");
        pnlActions.add(btnRefresh);
        btnViewAll.setText("Xem lịch hẹn");
        pnlActions.add(btnViewAll);
        pnlActions.add(lblDate);
        pnlTop.add(pnlActions, java.awt.BorderLayout.SOUTH);

        add(pnlTop, java.awt.BorderLayout.NORTH);

        scrollTable.setBorder(BorderFactory.createTitledBorder("Lịch hẹn gần đây"));
        scrollTable.setViewportView(tblData);
        add(scrollTable, java.awt.BorderLayout.CENTER);

        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
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