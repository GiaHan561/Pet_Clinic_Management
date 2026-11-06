package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {
    // 1. Tổng số khách hàng
    private static final String SQL_COUNT_CUSTOMERS =
            "SELECT COUNT(*) FROM CUSTOMERS";

    // 2. Lịch hẹn hôm nay (không tính lịch đã hủy)
    private static final String SQL_COUNT_TODAY_APPOINTMENTS =
            "SELECT COUNT(*) FROM APPOINTMENT "
            + "WHERE appointmentDate = CAST(GETDATE() AS DATE) "
            + "AND status <> N'Cancelled'";

    // 3. Doanh thu tháng này (chỉ tính thanh toán thành công)
    private static final String SQL_MONTH_REVENUE =
            "SELECT ISNULL(SUM(amount), 0) FROM PAYMENT "
            + "WHERE status = N'Success' "
            + "AND paymentDate >= DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1) "
            + "AND paymentDate < DATEADD(MONTH, 1, DATEFROMPARTS(YEAR(GETDATE()), MONTH(GETDATE()), 1))";

    // 4. 10 lịch hẹn mới nhất theo ngày giờ
    private static final String SQL_RECENT_APPOINTMENTS =
            "SELECT TOP 10 a.appointmentDate, a.appointmentTime, c.fullName, p.name, "
            + "       d.fullName AS doctorName, a.reason, "
            + "       CASE a.status "
            + "            WHEN N'Pending'   THEN N'Chờ xác nhận' "
            + "            WHEN N'Confirmed' THEN N'Đã xác nhận' "
            + "            WHEN N'Completed' THEN N'Hoàn thành' "
            + "            WHEN N'Cancelled' THEN N'Đã hủy' "
            + "            WHEN N'NoShow'    THEN N'Không đến' "
            + "            ELSE a.status END AS statusText "
            + "FROM APPOINTMENT a "
            + "JOIN CUSTOMERS c ON a.customerID = c.customerID "
            + "JOIN PETS p      ON a.petID = p.petID "
            + "JOIN DOCTORS d   ON a.doctorID = d.doctorID "
            + "ORDER BY a.appointmentDate DESC, a.appointmentTime DESC";

    public int countCustomers() throws SQLException {
        return queryInt(SQL_COUNT_CUSTOMERS);
    }

    public int countTodayAppointments() throws SQLException {
        return queryInt(SQL_COUNT_TODAY_APPOINTMENTS);
    }

    public double monthRevenue() throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_MONTH_REVENUE);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                BigDecimal v = rs.getBigDecimal(1);
                return v == null ? 0 : v.doubleValue();
            }
        }
        return 0;
    }

    /* Mỗi dòng: {thời gian, khách hàng, thú cưng, bác sĩ, lý do, trạng thái}. */
    public List<String[]> findRecentAppointments() throws SQLException {
        List<String[]> rows = new ArrayList<>();
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_RECENT_APPOINTMENTS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Date d = rs.getDate("appointmentDate");
                Time t = rs.getTime("appointmentTime");
                String when = (d == null ? "" : d.toLocalDate().format(dateFmt))
                        + " " + (t == null ? "" : t.toLocalTime().format(timeFmt));
                rows.add(new String[]{
                    when.trim(),
                    rs.getString("fullName"),
                    rs.getString("name"),
                    rs.getString("doctorName"),
                    rs.getString("reason"),
                    rs.getString("statusText")
                });
            }
        }
        return rows;
    }

    private int queryInt(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}