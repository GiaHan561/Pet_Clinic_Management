package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.VitalSign;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class VitalSignDAO {

    public int insert(VitalSign s) throws SQLException {
        String sql = """
            INSERT INTO VITAL_SIGN(visitID, weight, heartRate, respiratoryRate)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, s.getVisitID());
            ps.setBigDecimal(2, s.getWeight());
            DaoUtil.setInt(ps, 3, s.getHeartRate());
            DaoUtil.setInt(ps, 4, s.getRespiratoryRate());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public boolean update(VitalSign s) throws SQLException {
        String sql = """
            UPDATE VITAL_SIGN
            SET weight = ?, heartRate = ?, respiratoryRate = ?
            WHERE vitalSignID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, s.getWeight());
            DaoUtil.setInt(ps, 2, s.getHeartRate());
            DaoUtil.setInt(ps, 3, s.getRespiratoryRate());
            ps.setInt(4, s.getVitalSignID());

            return ps.executeUpdate() == 1;
        }
    }

    public List<VitalSign> findByRecordId(int recordID) throws SQLException {
        List<VitalSign> list = new ArrayList<>();

        String sql = """
            SELECT s.vitalSignID, s.visitID, s.weight, s.heartRate, s.respiratoryRate
            FROM VITAL_SIGN s
            JOIN MEDICAL_VISIT v ON v.visitID = s.visitID
            WHERE v.recordID = ?
            ORDER BY s.vitalSignID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VitalSign s = new VitalSign(
                        rs.getInt("visitID"),
                        rs.getBigDecimal("weight"),
                        DaoUtil.getInt(rs, "heartRate"),
                        DaoUtil.getInt(rs, "respiratoryRate")
                    );
                    s.setVitalSignID(rs.getInt("vitalSignID"));
                    list.add(s);
                }
            }
        }
        return list;
    }

    public boolean existsForVisit(int visitID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn,
                    "SELECT 1 FROM VITAL_SIGN WHERE visitID = ?", visitID);
        }
    }
}
