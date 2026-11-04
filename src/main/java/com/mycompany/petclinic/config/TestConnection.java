package com.mycompany.petclinic.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestConnection {
    public static void main(String[] args) {
        String sql = "SELECT * FROM CUSTOMERS";

        try (
            Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
        ) {

            System.out.println("KET NOI THANH CONG!");
            System.out.println("Database: " + conn.getCatalog());
            System.out.println("-----------------------------");
            System.out.println("DU LIEU CUSTOMERS:");

            while (rs.next()) {
                System.out.println(
                    rs.getInt("customerID") + " | " +
                    rs.getString("fullName") + " | " +
                    rs.getString("phone") + " | " +
                    rs.getString("email") + " | " +
                    rs.getString("address") + " | " +
                    rs.getString("createdTime")
                );
            }
        } catch (Exception e) {
            System.out.println("KET NOI HOAC TRUY VAN THAT BAI!");
            e.printStackTrace();
        }
    }
}