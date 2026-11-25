package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.MedicalRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecordLookupDAO {

    public List<MedicalRecord> search(Integer recordID, Integer petID) throws SQLException {
        List<MedicalRecord> list = new ArrayList<>();

        String sql = """
            SELECT recordID, petID, createdTime, generalNote, allergyNote, bloodType
            FROM MEDICAL_RECORD
            WHERE (? IS NULL OR recordID = ?)
              AND (? IS NULL OR petID = ?)
            ORDER BY recordID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            DaoUtil.setInt(ps, 1, recordID);
            DaoUtil.setInt(ps, 2, recordID);
            DaoUtil.setInt(ps, 3, petID);
            DaoUtil.setInt(ps, 4, petID);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp t = rs.getTimestamp("createdTime");
                    LocalDate created = t == null ? null : t.toLocalDateTime().toLocalDate();

                    MedicalRecord r = new MedicalRecord(
                        rs.getInt("petID"),
                        created,
                        rs.getString("generalNote"),
                        rs.getString("allergyNote"),
                        rs.getString("bloodType")
                    );
                    r.setMedicalRecordID(rs.getInt("recordID"));
                    list.add(r);
                }
            }
        }
        return list;
    }
}