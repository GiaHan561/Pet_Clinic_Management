package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Diagnosis;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiagnosisDAO {

    public int insert(Diagnosis d) throws SQLException {
        String sql = """
            INSERT INTO DIAGNOSIS(visitID, diagnosisName, severity, description)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, d.getVisitID());
            ps.setString(2, d.getDiagnosisName());
            ps.setString(3, d.getSeverity());
            ps.setString(4, d.getDescription());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public boolean update(Diagnosis d) throws SQLException {
        String sql = """
            UPDATE DIAGNOSIS
            SET diagnosisName = ?, severity = ?, description = ?
            WHERE diagnosisID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, d.getDiagnosisName());
            ps.setString(2, d.getSeverity());
            ps.setString(3, d.getDescription());
            ps.setInt(4, d.getDiagnosisID());

            return ps.executeUpdate() == 1;
        }
    }

    public List<Diagnosis> findByRecordId(int recordID) throws SQLException {
        List<Diagnosis> list = new ArrayList<>();

        String sql = """
            SELECT d.diagnosisID, d.visitID, d.diagnosisName, d.severity, d.description
            FROM DIAGNOSIS d
            JOIN MEDICAL_VISIT v ON v.visitID = d.visitID
            WHERE v.recordID = ?
            ORDER BY d.diagnosisID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Diagnosis d = new Diagnosis(
                        rs.getInt("visitID"),
                        rs.getString("diagnosisName"),
                        rs.getString("description"),
                        rs.getString("severity")
                    );
                    d.setDiagnosisID(rs.getInt("diagnosisID"));
                    list.add(d);
                }
            }
        }
        return list;
    }
}