package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    // 1. Thêm khách hàng, trả về ID tự tăng
    public int insert(Customer c) throws SQLException {
        String sql = """
            INSERT INTO CUSTOMERS(fullName, phone, email, address)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getFullName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // 2. Tìm khách hàng theo SĐT
    public Customer findByPhone(String phone) throws SQLException {
        String sql = """
            SELECT *
            FROM CUSTOMERS
            WHERE phone = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phone);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    // 3. Cập nhật SĐT, email, địa chỉ theo mã khách hàng
    public boolean update(Customer c) throws SQLException {
        String sql = """
            UPDATE CUSTOMERS
            SET phone = ?, email = ?, address = ?
            WHERE customerID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getPhone());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getAddress());
            ps.setInt(4, c.getCustomerID());

            return ps.executeUpdate() == 1;
        }
    }

    // 4. Xem tất cả khách hàng
    public List<Customer> findAll() throws SQLException {
        List<Customer> result = new ArrayList<>();

        String sql = """
            SELECT *
            FROM CUSTOMERS
            ORDER BY customerID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                result.add(map(rs));
            }
        }

        return result;
    }

    // 5. Kiểm tra khách hàng có tồn tại
    public boolean existsById(int customerId) throws SQLException {
        String sql = "SELECT 1 FROM CUSTOMERS WHERE customerID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

   // Chuyển một dòng ResultSet thành đối tượng Customer
    private Customer map(ResultSet rs) throws SQLException {

        Timestamp time = rs.getTimestamp("createdTime");

        LocalDateTime createdTime = null;

        if (time != null) {
            createdTime = time.toLocalDateTime();
        }

        return new Customer(
            rs.getInt("customerID"),
            rs.getString("fullName"),
            rs.getString("phone"),
            rs.getString("email"),
            rs.getString("address"),
            createdTime
        );
    }
}