
package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO {

    // Lấy toàn bộ dịch vụ
    public List<Service> findAll() throws SQLException {
        List<Service> list = new ArrayList<>();

        String sql = "SELECT serviceId, serviceName, description, "
                   + "price, duration, status FROM SERVICE "
                   + "ORDER BY serviceId";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapService(rs));
            }
        }

        return list;
    }

    // Tìm dịch vụ theo ID
    public Service findById(int id) throws SQLException {
        String sql = "SELECT * FROM SERVICE WHERE serviceId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapService(rs);
                }
            }
        }

        return null;
    }

    // Thêm dịch vụ
    public int insert(Service service) throws SQLException {
        String sql = "INSERT INTO SERVICE "
                   + "(serviceName, description, price, duration, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            setParameters(ps, service);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }

        return -1;
    }

    // Cập nhật dịch vụ
    public boolean update(Service service) throws SQLException {
        String sql = "UPDATE SERVICE SET "
                   + "serviceName = ?, description = ?, price = ?, "
                   + "duration = ?, status = ? "
                   + "WHERE serviceId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setParameters(ps, service);
            ps.setInt(6, service.getServiceId());

            return ps.executeUpdate() > 0;
        }
    }

    // Xóa dịch vụ
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM SERVICE WHERE serviceId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Tìm kiếm dịch vụ
    public List<Service> findByKeyword(String keyword)
            throws SQLException {

        List<Service> list = new ArrayList<>();

        String sql = "SELECT * FROM SERVICE "
                   + "WHERE CAST(serviceId AS VARCHAR(20)) LIKE ? "
                   + "OR serviceName LIKE ? "
                   + "OR description LIKE ? "
                   + "ORDER BY serviceId";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String value = "%" + keyword + "%";

            ps.setString(1, value);
            ps.setString(2, value);
            ps.setString(3, value);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapService(rs));
                }
            }
        }

        return list;
    }

    // Gán dữ liệu vào PreparedStatement
    private void setParameters(PreparedStatement ps, Service service)
            throws SQLException {

        ps.setString(1, service.getServiceName());
        ps.setString(2, service.getDescription());
        ps.setBigDecimal(3, service.getPrice());
        ps.setInt(4, service.getDuration());
        ps.setString(5, service.getStatus());
    }

    // Chuyển ResultSet thành Service
    private Service mapService(ResultSet rs) throws SQLException {
        return new Service(
                rs.getInt("serviceId"),
                rs.getString("serviceName"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                rs.getInt("duration"),
                rs.getString("status")
        );
    }
}

