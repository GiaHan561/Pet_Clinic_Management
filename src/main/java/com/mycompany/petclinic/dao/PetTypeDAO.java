package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.PetType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class PetTypeDAO {

    // 1. Thêm loài mới
    public boolean insert(PetType type) throws SQLException {

        String sql = """
            INSERT INTO PET_TYPES(typeName, description)
            VALUES (?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, type.getTypeName());
            ps.setString(2, type.getDescription());

            return ps.executeUpdate() == 1;
        }
    }

    // 2. Lấy danh sách các loài
    public List<PetType> findAll() throws SQLException {

        List<PetType> list = new ArrayList<>();

        String sql = """
            SELECT typeID, typeName, description
            FROM PET_TYPES
            ORDER BY typeName
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                PetType type = new PetType(
                    rs.getInt("typeID"),
                    rs.getString("typeName"),
                    rs.getString("description")
                );

                list.add(type);
            }
        }

        return list;
    }
}