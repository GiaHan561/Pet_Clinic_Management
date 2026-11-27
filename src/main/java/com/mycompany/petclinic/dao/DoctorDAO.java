package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Doctor;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DoctorDAO {

    // Câu SELECT dùng chung: gộp nhiều chuyên khoa thành 1 chuỗi "A, B, C"
    private static final String SELECT_DOCTOR =
            "SELECT d.doctorID, d.fullName, d.phone, d.email, "
          + "ISNULL((SELECT STRING_AGG(ds.specialization, N', ') "
          + "        FROM DOCTOR_SPECIALIZATION ds "
          + "        WHERE ds.doctorID = d.doctorID), N'') AS specialization, "
          + "d.licenseNumber, d.status "
          + "FROM DOCTORS d ";

    // 1. Lấy toàn bộ danh sách bác sĩ
    public List<Doctor> findAll() throws SQLException {
        List<Doctor> list = new ArrayList<>();
        String sql = SELECT_DOCTOR + "ORDER BY d.doctorID";

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
        String sql = SELECT_DOCTOR + "WHERE d.doctorID = ?";

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

    // 3. Thêm bác sĩ (bảng DOCTORS + bảng DOCTOR_SPECIALIZATION)
    public int insert(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO DOCTORS "
                   + "(fullName, phone, email, licenseNumber, status) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(
                    sql, Statement.RETURN_GENERATED_KEYS)) {

                setDoctorParameters(ps, doctor);

                if (ps.executeUpdate() > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            int newId = rs.getInt(1);
                            saveSpecializations(conn, newId,
                                    doctor.getSpecialization());
                            conn.commit();
                            return newId;
                        }
                    }
                }
                conn.rollback();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
        return -1;
    }

    // 4. Cập nhật bác sĩ
    public boolean update(Doctor doctor) throws SQLException {
        String sql = "UPDATE DOCTORS SET fullName = ?, phone = ?, "
                   + "email = ?, licenseNumber = ?, status = ? "
                   + "WHERE doctorID = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                setDoctorParameters(ps, doctor);
                ps.setInt(6, doctor.getDoctorID());

                boolean updated = ps.executeUpdate() > 0;

                if (updated) {
                    saveSpecializations(conn, doctor.getDoctorID(),
                            doctor.getSpecialization());
                }
                conn.commit();
                return updated;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 5. Xóa bác sĩ
    public boolean delete(int doctorID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // Lịch làm việc không có ON DELETE CASCADE nên xóa trước
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM DOCTOR_SCHEDULE WHERE doctorID = ?")) {
                    ps.setInt(1, doctorID);
                    ps.executeUpdate();
                }

                boolean deleted;
                // DOCTOR_SPECIALIZATION tự xóa theo (ON DELETE CASCADE)
                try (PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM DOCTORS WHERE doctorID = ?")) {
                    ps.setInt(1, doctorID);
                    deleted = ps.executeUpdate() > 0;
                }

                conn.commit();
                return deleted;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 6. Tìm kiếm bác sĩ
    public List<Doctor> findByKeyword(String keyword)
            throws SQLException {

        List<Doctor> list = new ArrayList<>();

        String sql = SELECT_DOCTOR
                   + "WHERE CAST(d.doctorID AS VARCHAR(20)) LIKE ? "
                   + "OR d.fullName LIKE ? "
                   + "OR d.phone LIKE ? "
                   + "OR EXISTS (SELECT 1 FROM DOCTOR_SPECIALIZATION x "
                   + "           WHERE x.doctorID = d.doctorID "
                   + "           AND x.specialization LIKE ?) "
                   + "ORDER BY d.doctorID";

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

    // 7. Ghi lại chuyên khoa: xóa cũ, thêm mới (tách theo dấu , hoặc ;)
    private void saveSpecializations(Connection conn, int doctorID,
                                     String specialization)
            throws SQLException {

        try (PreparedStatement del = conn.prepareStatement(
                "DELETE FROM DOCTOR_SPECIALIZATION WHERE doctorID = ?")) {
            del.setInt(1, doctorID);
            del.executeUpdate();
        }

        if (specialization == null || specialization.isBlank()) {
            return;
        }

        Set<String> items = new LinkedHashSet<>();
        for (String s : specialization.split("[,;]")) {
            String item = s.trim();
            if (!item.isEmpty()) {
                items.add(item);
            }
        }

        try (PreparedStatement ins = conn.prepareStatement(
                "INSERT INTO DOCTOR_SPECIALIZATION "
              + "(doctorID, specialization) VALUES (?, ?)")) {
            for (String item : items) {
                ins.setInt(1, doctorID);
                ins.setString(2, item);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }

    // 8. Gán dữ liệu từ ResultSet vào Doctor
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

    // 9. Gán tham số cho DOCTORS (5 cột, không còn specialization)
    private void setDoctorParameters(
            PreparedStatement ps, Doctor doctor)
            throws SQLException {

        ps.setString(1, doctor.getFullName());
        ps.setString(2, doctor.getPhone());
        ps.setString(3, doctor.getEmail());
        ps.setString(4, doctor.getLicenseNumber());
        ps.setString(5, doctor.getStatus());
    }
}
