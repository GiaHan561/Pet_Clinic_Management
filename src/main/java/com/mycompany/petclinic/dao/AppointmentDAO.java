
package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Appointment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Time;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    // 1. Thêm lịch hẹn mới
    // Kiểm tra khách hàng, thú cưng, bác sĩ và lịch làm việc
    public int insert(Appointment appointment) throws SQLException {

        String sql = """
            INSERT INTO APPOINTMENT
            (customerID, petID, doctorID,
             appointmentDate, appointmentTime,
             reason, note)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                validateBooking(conn, appointment, 0);

                try (PreparedStatement ps = conn.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setInt(1, appointment.getCustomerID());
                    ps.setInt(2, appointment.getPetID());
                    ps.setInt(3, appointment.getDoctorID());

                    ps.setDate(4,
                        Date.valueOf(appointment.getAppointmentDate()));

                    ps.setTime(5,
                        Time.valueOf(appointment.getAppointmentTime()));

                    ps.setString(6, appointment.getReason());
                    ps.setString(7, appointment.getNote());

                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {

                        if (rs.next()) {
                            int newID = rs.getInt(1);
                            conn.commit();
                            return newID;
                        }
                    }
                }

                conn.rollback();
                return -1;

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 2. Lấy toàn bộ lịch hẹn
    public List<Appointment> findAll() throws SQLException {

        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT appointmentID, customerID, petID, doctorID,
                   appointmentDate, appointmentTime,
                   reason, note, status
            FROM APPOINTMENT
            ORDER BY appointmentDate DESC, appointmentTime DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapAppointment(rs));
            }
        }

        return list;
    }

    // 3. Tìm lịch hẹn theo ID
    public Appointment findById(int appointmentID)
            throws SQLException {

        String sql = """
            SELECT appointmentID, customerID, petID, doctorID,
                   appointmentDate, appointmentTime,
                   reason, note, status
            FROM APPOINTMENT
            WHERE appointmentID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, appointmentID);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return mapAppointment(rs);
                }
            }
        }

        return null;
    }

    // 4. Cập nhật lịch hẹn
    public boolean update(Appointment appointment)
            throws SQLException {

        String sql = """
            UPDATE APPOINTMENT
            SET customerID = ?,
                petID = ?,
                doctorID = ?,
                appointmentDate = ?,
                appointmentTime = ?,
                reason = ?,
                note = ?
            WHERE appointmentID = ?
              AND status NOT IN (N'Completed', N'Cancelled', N'NoShow')
            """;

        try (Connection conn = DatabaseConnection.getConnection()) {

            conn.setAutoCommit(false);

            try {
                validateBooking(
                    conn, appointment,
                    appointment.getAppointmentID()
                );

                try (PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, appointment.getCustomerID());
                    ps.setInt(2, appointment.getPetID());
                    ps.setInt(3, appointment.getDoctorID());

                    ps.setDate(4,
                        Date.valueOf(appointment.getAppointmentDate()));

                    ps.setTime(5,
                        Time.valueOf(appointment.getAppointmentTime()));

                    ps.setString(6, appointment.getReason());
                    ps.setString(7, appointment.getNote());

                    ps.setInt(8, appointment.getAppointmentID());

                    boolean updated = ps.executeUpdate() == 1;

                    conn.commit();
                    return updated;
                }

            } catch (SQLException | RuntimeException e) {
                conn.rollback();
                throw e;
            }
        }
    }

    // 5. Hủy lịch hẹn và lưu lý do
    public boolean cancel(int appointmentID, String cancelReason)
            throws SQLException {

        String sql = """
            UPDATE APPOINTMENT
            SET status = N'Cancelled',
                note = CONCAT(
                    COALESCE(note + N' | ', N''),
                    N'Cancel reason: ',
                    ?
                )
            WHERE appointmentID = ?
              AND status NOT IN (N'Completed', N'Cancelled', N'NoShow')
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cancelReason);
            ps.setInt(2, appointmentID);

            return ps.executeUpdate() == 1;
        }
    }

    // 6. Kiểm tra bác sĩ có làm việc vào ngày và giờ đó không
    public boolean isDoctorAvailable(
            int doctorID,
            LocalDate date,
            LocalTime time) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM DOCTOR_SCHEDULE ds
            JOIN DOCTORS d ON ds.doctorID = d.doctorID
            WHERE ds.doctorID = ?
              AND ds.day_of_week = ?
              AND ds.startTime <= ?
              AND ds.endTime > ?
              AND d.status = N'Active'
            """;

        // Java: Monday = 1, Sunday = 7
        // Database: Monday = 2, Sunday = 8
        int dayOfWeek = date.getDayOfWeek().getValue() + 1;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, doctorID);
            ps.setInt(2, dayOfWeek);
            ps.setTime(3, Time.valueOf(time));
            ps.setTime(4, Time.valueOf(time));

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // 7. Kiểm tra lịch hẹn bị trùng
    // Loại trừ lịch hẹn hiện tại khi cập nhật
    private boolean isAppointmentDuplicated(
            Connection conn,
            int doctorID,
            int petID,
            LocalDate date,
            LocalTime time,
            int excludeAppointmentID) throws SQLException {

        String sql = """
            SELECT COUNT(*)
            FROM APPOINTMENT
            WHERE appointmentDate = ?
              AND appointmentTime = ?
              AND (doctorID = ? OR petID = ?)
              AND appointmentID <> ?
              AND status NOT IN (N'Cancelled', N'NoShow')
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setTime(2, Time.valueOf(time));
            ps.setInt(3, doctorID);
            ps.setInt(4, petID);
            ps.setInt(5, excludeAppointmentID);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    // 8. Kiểm tra toàn bộ điều kiện trước khi lưu lịch
    private void validateBooking(
            Connection conn,
            Appointment appointment,
            int excludeAppointmentID) throws SQLException {

        if (appointment.getAppointmentDate() == null
                || appointment.getAppointmentTime() == null) {
            throw new IllegalArgumentException(
                "Ngày và giờ hẹn không được để trống!"
            );
        }

        if (!appointment.getAppointmentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Ngày hẹn phải sau ngày hôm nay!"
            );
        }

        // Kiểm tra thú cưng thuộc đúng khách hàng
        String petSql = """
            SELECT 1
            FROM PETS
            WHERE petID = ? AND customerID = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(petSql)) {

            ps.setInt(1, appointment.getPetID());
            ps.setInt(2, appointment.getCustomerID());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException(
                        "Thú cưng không thuộc khách hàng đã chọn!"
                    );
                }
            }
        }

        // Kiểm tra bác sĩ có lịch làm việc
        String doctorSql = """
            SELECT COUNT(*)
            FROM DOCTOR_SCHEDULE ds
            JOIN DOCTORS d ON ds.doctorID = d.doctorID
            WHERE ds.doctorID = ?
              AND ds.day_of_week = ?
              AND ds.startTime <= ?
              AND ds.endTime > ?
              AND d.status = N'Active'
            """;

        int dayOfWeek =
            appointment.getAppointmentDate()
                       .getDayOfWeek().getValue() + 1;

        try (PreparedStatement ps = conn.prepareStatement(doctorSql)) {

            ps.setInt(1, appointment.getDoctorID());
            ps.setInt(2, dayOfWeek);

            ps.setTime(3,
                Time.valueOf(appointment.getAppointmentTime()));

            ps.setTime(4,
                Time.valueOf(appointment.getAppointmentTime()));

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next() || rs.getInt(1) == 0) {
                    throw new IllegalArgumentException(
                        "Bác sĩ không làm việc vào thời gian này!"
                    );
                }
            }
        }

        // Kiểm tra lịch bị trùng
        if (isAppointmentDuplicated(
                conn,
                appointment.getDoctorID(),
                appointment.getPetID(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                excludeAppointmentID)) {

            throw new IllegalArgumentException(
                "Bác sĩ hoặc thú cưng đã có lịch hẹn vào thời gian này!"
            );
        }
    }

    // 9. Tìm kiếm lịch hẹn theo ID, lý do hoặc ghi chú
    public List<Appointment> findByKeyword(String keyword)
            throws SQLException {

        List<Appointment> list = new ArrayList<>();

        String sql = """
            SELECT appointmentID, customerID, petID, doctorID,
                   appointmentDate, appointmentTime,
                   reason, note, status
            FROM APPOINTMENT
            WHERE CAST(appointmentID AS NVARCHAR(20)) LIKE ?
               OR reason LIKE ?
               OR note LIKE ?
            ORDER BY appointmentDate DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String value = "%" + keyword.trim() + "%";

            ps.setString(1, value);
            ps.setString(2, value);
            ps.setString(3, value);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapAppointment(rs));
                }
            }
        }

        return list;
    }

    // 10. Chuyển dữ liệu SQL thành Appointment object
    private Appointment mapAppointment(ResultSet rs)
            throws SQLException {

        return new Appointment(
            rs.getInt("appointmentID"),
            rs.getInt("customerID"),
            rs.getInt("petID"),
            rs.getInt("doctorID"),
            rs.getDate("appointmentDate").toLocalDate(),
            rs.getTime("appointmentTime").toLocalTime(),
            rs.getString("reason"),
            rs.getString("note"),
            rs.getString("status")
        );
    }
}

