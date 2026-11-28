package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.AppointmentService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppointmentServiceDAO {

    // Lấy tất cả dịch vụ đã được gắn vào các lịch hẹn
    public List<AppointmentService> findAll() {
        List<AppointmentService> list = new ArrayList<>();

        String sql = """
            SELECT aps.appointmentID,
                   aps.serviceID,
                   s.serviceName,
                   aps.quantity,
                   aps.unitPrice
            FROM APPOINTMENT_SERVICE aps
            JOIN SERVICES s ON aps.serviceID = s.serviceID
            ORDER BY aps.appointmentID, aps.serviceID
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                AppointmentService item = new AppointmentService(
                    rs.getInt("appointmentID"),
                    rs.getInt("serviceID"),
                    rs.getString("serviceName"),
                    rs.getInt("quantity"),
                    rs.getBigDecimal("unitPrice")
                );
                list.add(item);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lấy danh sách dịch vụ thuộc một lịch hẹn
    public List<AppointmentService> findByAppointmentId(int appointmentID) {
        List<AppointmentService> list = new ArrayList<>();

        String sql = """
            SELECT aps.appointmentID,
                   aps.serviceID,
                   s.serviceName,
                   aps.quantity,
                   aps.unitPrice
            FROM APPOINTMENT_SERVICE aps
            JOIN SERVICES s ON aps.serviceID = s.serviceID
            WHERE aps.appointmentID = ?
            ORDER BY aps.serviceID
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AppointmentService item = new AppointmentService(
                        rs.getInt("appointmentID"),
                        rs.getInt("serviceID"),
                        rs.getString("serviceName"),
                        rs.getInt("quantity"),
                        rs.getBigDecimal("unitPrice")
                    );
                    list.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // Lấy đơn giá của dịch vụ đang hoạt động trong bảng SERVICES
    public BigDecimal getServicePrice(int serviceID) {
        String sql = """
            SELECT price
            FROM SERVICES
            WHERE serviceID = ?
              AND status = N'Active'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, serviceID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("price");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    // Thêm dịch vụ vào lịch hẹn với đối tượng đã có sẵn đơn giá
    public boolean insert(AppointmentService item) {
        if (item == null || item.getQuantity() <= 0
                || item.getUnitPrice() == null) {
            return false;
        }

        String sql = """
            INSERT INTO APPOINTMENT_SERVICE
                (appointmentID, serviceID, quantity, unitPrice)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, item.getAppointmentID());
            ps.setInt(2, item.getServiceID());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Thêm dịch vụ bằng mã lịch hẹn, mã dịch vụ và số lượng.
    // Đơn giá được lấy tự động từ bảng SERVICES.
    public boolean insert(int appointmentID, int serviceID, int quantity) {
        if (quantity <= 0) {
            return false;
        }

        BigDecimal price = getServicePrice(serviceID);
        if (price == null) {
            return false;
        }

        AppointmentService item = new AppointmentService(
            appointmentID,
            serviceID,
            quantity,
            price
        );

        return insert(item);
    }

    // Cập nhật số lượng dịch vụ
    public boolean updateQuantity(
            int appointmentID, int serviceID, int quantity) {

        if (quantity <= 0) {
            return false;
        }

        String sql = """
            UPDATE APPOINTMENT_SERVICE
            SET quantity = ?
            WHERE appointmentID = ? AND serviceID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setInt(2, appointmentID);
            ps.setInt(3, serviceID);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Xóa dịch vụ khỏi lịch hẹn
    public boolean delete(int appointmentID, int serviceID) {
        String sql = """
            DELETE FROM APPOINTMENT_SERVICE
            WHERE appointmentID = ? AND serviceID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);
            ps.setInt(2, serviceID);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Tính tổng tiền dịch vụ của một lịch hẹn
    public BigDecimal getTotalByAppointmentId(int appointmentID) {
        String sql = """
            SELECT COALESCE(SUM(quantity * unitPrice), 0) AS total
            FROM APPOINTMENT_SERVICE
            WHERE appointmentID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal total = rs.getBigDecimal("total");
                    return total != null ? total : BigDecimal.ZERO;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }
}
