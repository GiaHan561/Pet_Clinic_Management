package com.mycompany.petclinic.dao;

import com.mycompany.petclinic.config.DatabaseConnection;
import com.mycompany.petclinic.model.Payment_Method;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class PaymentMethodDAO {

    public List<Payment_Method> findAll() throws SQLException {

        List<Payment_Method> list = new ArrayList<>();

        String sql = """
            SELECT paymentMethodID, typeName, description
            FROM PAYMENT_METHOD
            ORDER BY paymentMethodID
            """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Payment_Method method = new Payment_Method(
                    rs.getInt("paymentMethodID"),
                    rs.getString("typeName"),
                    rs.getString("description")
                );

                list.add(method);
            }
        }

        return list;
    }
}

