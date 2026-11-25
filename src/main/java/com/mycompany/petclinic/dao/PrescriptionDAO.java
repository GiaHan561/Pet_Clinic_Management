package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Prescription;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {

    public int insert(Prescription p) throws SQLException {
        String sql = """
            INSERT INTO PRESCRIPTION(visitID, doctorID, prescriptionDate, note)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, p.getVisitID());
            ps.setInt(2, p.getDoctorID());
            ps.setTimestamp(3, Timestamp.valueOf(p.getPrescriptionDate()));
            ps.setString(4, p.getNote());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public boolean update(Prescription p) throws SQLException {
        String sql = """
            UPDATE PRESCRIPTION
            SET doctorID = ?, prescriptionDate = ?, note = ?
            WHERE prescriptionID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, p.getDoctorID());
            ps.setTimestamp(2, Timestamp.valueOf(p.getPrescriptionDate()));
            ps.setString(3, p.getNote());
            ps.setInt(4, p.getPrescriptionID());

            return ps.executeUpdate() == 1;
        }
    }

    public List<Prescription> findByRecordId(int recordID) throws SQLException {
        List<Prescription> list = new ArrayList<>();

        String sql = """
            SELECT p.prescriptionID, p.visitID, p.doctorID, p.prescriptionDate, p.note
            FROM PRESCRIPTION p
            JOIN MEDICAL_VISIT v ON v.visitID = p.visitID
            WHERE v.recordID = ?
            ORDER BY p.prescriptionDate DESC, p.prescriptionID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("prescriptionDate");
                    Prescription p = new Prescription(
                        rs.getInt("visitID"),
                        rs.getInt("doctorID"),
                        t == null ? null : t.toLocalDateTime(),
                        rs.getString("note")
                    );
                    p.setPrescriptionID(rs.getInt("prescriptionID"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    public boolean existsForVisit(int visitID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn,
                    "SELECT 1 FROM PRESCRIPTION WHERE visitID = ?", visitID);
        }
    }

    public boolean belongsToRecord(int prescriptionID, int recordID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn, """
                SELECT 1
                FROM PRESCRIPTION p
                JOIN MEDICAL_VISIT v ON v.visitID = p.visitID
                WHERE p.prescriptionID = ? AND v.recordID = ?
                """, prescriptionID, recordID);
        }
    }
}