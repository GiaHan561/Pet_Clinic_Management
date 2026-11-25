
package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    // 1. Lấy toàn bộ danh sách bác sĩ
    public List<Doctor> findAll() throws SQLException {
        List<Doctor> list = new ArrayList<>();

        String sql = "SELECT doctorID, fullName, phone, email, "
                   + "specialization, licenseNumber, status "
                   + "FROM DOCTOR";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapDoctor(rs));
            }
        }

        return list;
    }

    // 2. Tìm bác sĩ theo ID
    public Doctor findById(int doctorID) throws SQLException {
        String sql = "SELECT doctorID, fullName, phone, email, "
                   + "specialization, licenseNumber, status "
                   + "FROM DOCTOR WHERE doctorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDoctor(rs);
                }
            }
        }

        return null;
    }

    // 3. Thêm bác sĩ
    public int insert(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO DOCTOR "
                   + "(fullName, phone, email, specialization, "
                   + "licenseNumber, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            setDoctorParameters(ps, doctor);

            int rows = ps.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }

        return -1;
    }

    // 4. Cập nhật bác sĩ
    public boolean update(Doctor doctor) throws SQLException {
        String sql = "UPDATE DOCTOR SET fullName = ?, phone = ?, "
                   + "email = ?, specialization = ?, "
                   + "licenseNumber = ?, status = ? "
                   + "WHERE doctorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setDoctorParameters(ps, doctor);
            ps.setInt(7, doctor.getDoctorID());

            return ps.executeUpdate() > 0;
        }
    }

    // 5. Xóa bác sĩ
    public boolean delete(int doctorID) throws SQLException {
        String sql = "DELETE FROM DOCTOR WHERE doctorID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);
            return ps.executeUpdate() > 0;
        }
    }

    // 6. Tìm kiếm bác sĩ
    public List<Doctor> findByKeyword(String keyword)
            throws SQLException {

        List<Doctor> list = new ArrayList<>();

        String sql = "SELECT doctorID, fullName, phone, email, "
                   + "specialization, licenseNumber, status "
                   + "FROM DOCTOR "
                   + "WHERE CAST(doctorID AS VARCHAR(20)) LIKE ? "
                   + "OR fullName LIKE ? "
                   + "OR phone LIKE ? "
                   + "OR specialization LIKE ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String value = "%" + keyword + "%";

            for (int i = 1; i <= 4; i++) {
                ps.setString(i, value);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDoctor(rs));
                }
            }
        }

        return list;
    }

    // 7. Gán dữ liệu từ ResultSet vào Doctor
    private Doctor mapDoctor(ResultSet rs) throws SQLException {
        return new Doctor(
                rs.getInt("doctorID"),
                rs.getString("fullName"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("specialization"),
                rs.getString("licenseNumber"),
                rs.getString("status")
        );
    }

    // 8. Gán tham số khi thêm hoặc cập nhật
    private void setDoctorParameters(
            PreparedStatement ps, Doctor doctor)
            throws SQLException {

        ps.setString(1, doctor.getFullName());
        ps.setString(2, doctor.getPhone());
        ps.setString(3, doctor.getEmail());
        ps.setString(4, doctor.getSpecialization());
        ps.setString(5, doctor.getLicenseNumber());
        ps.setString(6, doctor.getStatus());
    }
}

