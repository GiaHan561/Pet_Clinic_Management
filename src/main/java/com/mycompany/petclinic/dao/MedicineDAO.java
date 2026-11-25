package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Medicine;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO {

    private static final String SELECT_BASE = """
        SELECT m.medicineID, m.name, m.genericName, m.description, m.dosage,
               m.unit, m.quantity, m.price, m.manufactureDate, m.expiryDay,
               ISNULL((SELECT STRING_AGG(c.category, N', ')
                       FROM MEDICINE_CATEGORY c
                       WHERE c.medicineID = m.medicineID), N'') AS categories
        FROM MEDICINE m
        """;

    public List<Medicine> search(String keyword) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim();
        boolean all = kw.isEmpty();

        String sql = SELECT_BASE + (all ? "" : """
            WHERE m.medicineID = TRY_CAST(? AS INT)
               OR m.name LIKE ?
               OR m.genericName LIKE ?
               OR EXISTS (SELECT 1 FROM MEDICINE_CATEGORY c
                          WHERE c.medicineID = m.medicineID AND c.category LIKE ?)
            """) + " ORDER BY m.medicineID";

        List<Medicine> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!all) {
                String like = "%" + kw + "%";
                ps.setString(1, kw);
                ps.setString(2, like);
                ps.setString(3, like);
                ps.setString(4, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Medicine findById(int medicineID) throws SQLException {
        String sql = SELECT_BASE + " WHERE m.medicineID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicineID);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Medicine map(ResultSet rs) throws SQLException {
        Date made = rs.getDate("manufactureDate");
        Date expiry = rs.getDate("expiryDay");
        LocalDate manufactureDate = made == null ? null : made.toLocalDate();
        LocalDate expiryDate = expiry == null ? null : expiry.toLocalDate();

        return new Medicine(
            rs.getInt("medicineID"),
            rs.getString("categories"),
            rs.getString("name"),
            rs.getString("genericName"),
            rs.getString("description"),
            rs.getString("dosage"),
            rs.getString("unit"),
            rs.getInt("quantity"),
            rs.getBigDecimal("price"),
            manufactureDate,
            expiryDate
        );
    }
}