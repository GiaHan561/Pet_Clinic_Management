package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Vaccination;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VaccinationDAO {

    public int insert(Vaccination v) throws SQLException {
        String sql = """
            INSERT INTO VACCINATION(visitID, vaccinID, injectionDate, nextInjectionDate)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, v.getVisitID());
            ps.setInt(2, v.getVaccineID());
            ps.setDate(3, Date.valueOf(v.getInjectionDate()));
            if (v.getNextInjectionDate() == null) {
                ps.setNull(4, java.sql.Types.DATE);
            } else {
                ps.setDate(4, Date.valueOf(v.getNextInjectionDate()));
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public boolean update(Vaccination v) throws SQLException {
        String sql = """
            UPDATE VACCINATION
            SET injectionDate = ?, nextInjectionDate = ?
            WHERE vaccinationID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(v.getInjectionDate()));
            if (v.getNextInjectionDate() == null) {
                ps.setNull(2, java.sql.Types.DATE);
            } else {
                ps.setDate(2, Date.valueOf(v.getNextInjectionDate()));
            }
            ps.setInt(3, v.getVaccinationID());

            return ps.executeUpdate() == 1;
        }
    }

    public List<Vaccination> findByRecordId(int recordID) throws SQLException {
        List<Vaccination> list = new ArrayList<>();

        String sql = """
            SELECT va.vaccinationID, va.visitID, va.vaccinID, vc.name,
                   va.injectionDate, va.nextInjectionDate
            FROM VACCINATION va
            JOIN VACCINE vc ON vc.vaccinID = va.vaccinID
            JOIN MEDICAL_VISIT v ON v.visitID = va.visitID
            WHERE v.recordID = ?
            ORDER BY va.injectionDate DESC, va.vaccinationID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, recordID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date next = rs.getDate("nextInjectionDate");
                    Vaccination v = new Vaccination(
                        rs.getInt("visitID"),
                        rs.getInt("vaccinID"),
                        rs.getDate("injectionDate").toLocalDate(),
                        next == null ? null : next.toLocalDate()
                    );
                    v.setVaccinationID(rs.getInt("vaccinationID"));
                    v.setVaccineName(rs.getString("name"));
                    list.add(v);
                }
            }
        }
        return list;
    }

    public boolean existsForVisit(int visitID, int vaccineID) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return DaoUtil.exists(conn, """
                SELECT 1 FROM VACCINATION WHERE visitID = ? AND vaccinID = ?
                """, visitID, vaccineID);
        }
    }
}