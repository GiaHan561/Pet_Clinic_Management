package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.MedicalVisit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MedicalVisitDAO {

    public int insert(MedicalVisit v) throws SQLException {
        String sql = """
            INSERT INTO MEDICAL_VISIT
            (recordID, doctorID, appointmentID, petID, visitTime, reason, status)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, v.getMedicalRecordID());
            ps.setInt(2, v.getDoctorID());
            DaoUtil.setInt(ps, 3, v.getAppointmentID());
            ps.setInt(4, v.getPetID());
            ps.setTimestamp(5, Timestamp.valueOf(v.getVisitTime()));
            ps.setString(6, v.getReason());
            ps.setString(7, v.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public boolean update(MedicalVisit v) throws SQLException {
        String sql = """
            UPDATE MEDICAL_VISIT
            SET doctorID = ?, appointmentID = ?, visitTime = ?,
                reason = ?, status = ?
            WHERE visitID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, v.getDoctorID());
            DaoUtil.setInt(ps, 2, v.getAppointmentID());
            ps.setTimestamp(3, Timestamp.valueOf(v.getVisitTime()));
            ps.setString(4, v.getReason());
            ps.setString(5, v.getStatus());
            ps.setInt(6, v.getVisitID());

            return ps.executeUpdate() == 1;
        }
    }

    public List<MedicalVisit> findByRecordId(int recordID) throws SQLException {
        List<MedicalVisit> list = new ArrayList<>();

        String sql = """
            SELECT visitID, recordID, doctorID, appointmentID,
                   petID, visitTime, reason, status
            FROM MEDICAL_VISIT
            WHERE recordID = ?
            ORDER BY visitTime DESC, visitID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public boolean doctorExists(int doctorID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn, "SELECT 1 FROM DOCTORS WHERE doctorID = ?", doctorID);
        }
    }

    public boolean appointmentBelongsToPet(int appointmentID, int petID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn,
                    "SELECT 1 FROM APPOINTMENT WHERE appointmentID = ? AND petID = ?",
                    appointmentID, petID);
        }
    }

    public boolean visitBelongsToRecord(int visitID, int recordID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn,
                    "SELECT 1 FROM MEDICAL_VISIT WHERE visitID = ? AND recordID = ?",
                    visitID, recordID);
        }
    }

    private MedicalVisit map(ResultSet rs) throws SQLException {
        Timestamp t = rs.getTimestamp("visitTime");

        MedicalVisit v = new MedicalVisit(
            rs.getInt("recordID"),
            rs.getInt("doctorID"),
            DaoUtil.getInt(rs, "appointmentID"),
            rs.getInt("petID"),
            t == null ? null : t.toLocalDateTime(),
            rs.getString("reason"),
            rs.getString("status")
        );
        v.setVisitID(rs.getInt("visitID"));
        return v;
    }
}