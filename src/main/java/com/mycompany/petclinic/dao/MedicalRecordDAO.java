package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.MedicalRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalRecordDAO {

    private static final String SELECT_BASE = """
        SELECT recordID, petID, createdTime,
               generalNote, allergyNote, bloodType
        FROM MEDICAL_RECORD
        """;

    public int insert(MedicalRecord r) throws SQLException {
        String sql = """
            INSERT INTO MEDICAL_RECORD(petID, generalNote, allergyNote, bloodType)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, r.getPetID());
            ps.setString(2, r.getNote());
            ps.setString(3, r.getAllergy());
            ps.setString(4, r.getBloodType());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public List<MedicalRecord> findAll() throws SQLException {
        return query(SELECT_BASE + " ORDER BY recordID DESC", null);
    }

    public List<MedicalRecord> findByPetId(int petID) throws SQLException {
        return query(SELECT_BASE + " WHERE petID = ? ORDER BY recordID DESC", petID);
    }

    public boolean petExists(int petID) throws SQLException {
        return exists("SELECT 1 FROM PETS WHERE petID = ?", petID);
    }

    public boolean hasRecord(int petID) throws SQLException {
        return exists("SELECT 1 FROM MEDICAL_RECORD WHERE petID = ?", petID);
    }

    private List<MedicalRecord> query(String sql, Integer petID) throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (petID != null) {
                ps.setInt(1, petID);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    private boolean exists(String sql, int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private MedicalRecord map(ResultSet rs) throws SQLException {
        Timestamp t = rs.getTimestamp("createdTime");
        LocalDate createdDate = (t == null) ? null : t.toLocalDateTime().toLocalDate();

        MedicalRecord r = new MedicalRecord(
            rs.getInt("petID"),
            createdDate,
            rs.getString("generalNote"),
            rs.getString("allergyNote"),
            rs.getString("bloodType")
        );
        r.setMedicalRecordID(rs.getInt("recordID"));
        return r;
    }
}
