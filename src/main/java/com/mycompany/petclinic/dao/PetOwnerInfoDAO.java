package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.PetOwnerInfo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PetOwnerInfoDAO {

    public PetOwnerInfo findByPetId(int petID) throws SQLException {
        String sql = """
            SELECT p.name AS petName, t.typeName, p.gender, p.date_of_birth,
                   p.weight, p.note AS petNote,
                   c.fullName, c.phone, c.email, c.address
            FROM PETS p
            JOIN PET_TYPES t ON t.typeID = p.typeID
            JOIN CUSTOMERS c ON c.customerID = p.customerID
            WHERE p.petID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, petID);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Date dob = rs.getDate("date_of_birth");
                return new PetOwnerInfo(
                    rs.getString("petName"),
                    rs.getString("typeName"),
                    rs.getString("gender"),
                    dob == null ? null : dob.toLocalDate(),
                    rs.getBigDecimal("weight"),
                    rs.getString("petNote"),
                    rs.getString("fullName"),
                    rs.getString("phone"),
                    rs.getString("email"),
                    rs.getString("address")
                );
            }
        }
    }
}