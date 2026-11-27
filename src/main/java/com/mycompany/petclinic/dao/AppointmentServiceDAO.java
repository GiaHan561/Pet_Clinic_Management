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

    // Lấy đơn giá hiện hành của dịch vụ từ bảng SERVICES (chỉ dịch vụ Active)
    // Trả về null nếu dịch vụ không tồn tại hoặc đã ngừng cung cấp
    public BigDecimal getServicePrice(int serviceID) {
        String sql = """
            SELECT price
            FROM SERVICES
            WHERE serviceID = ? AND status = N'Active'
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

    // Thêm dịch vụ vào lịch hẹn
    // Đơn giá không do người dùng nhập mà tự lấy từ bảng SERVICES
    public boolean insert(int appointmentID, int serviceID, int quantity) {
        String sql = """
            INSERT INTO APPOINTMENT_SERVICE
                (appointmentID, serviceID, quantity, unitPrice)
            SELECT ?, serviceID, ?, price
            FROM SERVICES
            WHERE serviceID = ? AND status = N'Active'
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);
            ps.setInt(2, quantity);
            ps.setInt(3, serviceID);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Cập nhật số lượng dịch vụ
    public boolean updateQuantity(
            int appointmentID, int serviceID, int quantity) {

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

    // Tính tổng tiền dịch vụ của lịch hẹn
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
                    return rs.getBigDecimal("total");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return BigDecimal.ZERO;
    }
}
