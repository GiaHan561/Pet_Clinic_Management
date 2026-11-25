package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Vaccine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VaccineDAO {

    private static final String SELECT_BASE = """
        SELECT vaccinID, name, manufacturer, description, price, rcmInterval
        FROM VACCINE
        """;

    public List<Vaccine> search(String keyword) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim();
        boolean all = kw.isEmpty();

        String sql = SELECT_BASE + (all ? "" : """
            WHERE vaccinID = TRY_CAST(? AS INT)
               OR name LIKE ?
               OR manufacturer LIKE ?
            """) + " ORDER BY vaccinID";

        List<Vaccine> list = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!all) {
                String like = "%" + kw + "%";
                ps.setString(1, kw);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public Vaccine findById(int vaccineID) throws SQLException {
        String sql = SELECT_BASE + " WHERE vaccinID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, vaccineID);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    private Vaccine map(ResultSet rs) throws SQLException {
        return new Vaccine(
            rs.getInt("vaccinID"),
            rs.getString("name"),
            rs.getString("manufacturer"),
            rs.getString("description"),
            rs.getBigDecimal("price"),
            DaoUtil.getInt(rs, "rcmInterval")
        );
    }
}