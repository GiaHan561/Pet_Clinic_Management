package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Pet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PetDAO {

    // 1. Đăng ký thú cưng
    public boolean insert(Pet pet) throws SQLException {

        String sql = """
            INSERT INTO PETS
            (customerID, typeID, name, gender,
             date_of_birth, weight, note)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pet.getCustomerID());
            ps.setInt(2, pet.getTypeID());
            ps.setString(3, pet.getName());
            ps.setString(4, pet.getGender());

            if (pet.getDateOfBirth() == null) {
                ps.setNull(5, Types.DATE);
            } else {
                ps.setDate(5,
                    java.sql.Date.valueOf(pet.getDateOfBirth()));
            }

            ps.setBigDecimal(6, pet.getWeight());
            ps.setString(7, pet.getNote());

            return ps.executeUpdate() == 1;
        }
    }

    // 2. Lấy danh sách tất cả thú cưng
    public List<Pet> findAll() throws SQLException {

        List<Pet> list = new ArrayList<>();

        String sql = """
            SELECT petID, customerID, typeID, name, gender,
                   date_of_birth, weight, note
            FROM PETS
            ORDER BY petID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapPet(rs));
            }
        }

        return list;
    }

    // 3. Lấy danh sách thú cưng của một khách hàng
    public List<Pet> findByCustomerId(int customerID)
            throws SQLException {

        List<Pet> list = new ArrayList<>();

        String sql = """
            SELECT petID, customerID, typeID, name, gender,
                   date_of_birth, weight, note
            FROM PETS
            WHERE customerID = ?
            ORDER BY petID DESC
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, customerID);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    list.add(mapPet(rs));
                }
            }
        }

        return list;
    }

    // 4. Cập nhật cân nặng và ghi chú sức khỏe
    public boolean update(Pet pet) throws SQLException {

        String sql = """
            UPDATE PETS
            SET weight = ?, note = ?
            WHERE petID = ?
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBigDecimal(1, pet.getWeight());
            ps.setString(2, pet.getNote());
            ps.setInt(3, pet.getPetID());

            return ps.executeUpdate() == 1;
        }
    }

    // 5. Đếm tổng số thú cưng
    public int countAll() throws SQLException {

        String sql = "SELECT COUNT(*) AS totalPets FROM PETS";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("totalPets");
            }
        }

        return 0;
    }

    // 6. Thống kê số lượng thú cưng theo từng loài
    public Map<String, Integer> countByType() throws SQLException {

        Map<String, Integer> result = new LinkedHashMap<>();

        String sql = """
            SELECT t.typeName, COUNT(p.petID) AS totalPets
            FROM PET_TYPES t
            LEFT JOIN PETS p ON t.typeID = p.typeID
            GROUP BY t.typeID, t.typeName
            ORDER BY t.typeName
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                String typeName = rs.getString("typeName");
                int quantity = rs.getInt("totalPets");

                result.put(typeName, quantity);
            }
        }

        return result;
    }

    // Chuyển dữ liệu SQL thành đối tượng Pet
    private Pet mapPet(ResultSet rs) throws SQLException {

        java.sql.Date sqlDate = rs.getDate("date_of_birth");
        LocalDate birthDate = null;

        if (sqlDate != null) {
            birthDate = sqlDate.toLocalDate();
        }

        return new Pet(
            rs.getInt("petID"),
            rs.getInt("customerID"),
            rs.getInt("typeID"),
            rs.getString("name"),
            rs.getString("gender"),
            birthDate,
            rs.getBigDecimal("weight"),
            rs.getString("note")
        );
    }
}