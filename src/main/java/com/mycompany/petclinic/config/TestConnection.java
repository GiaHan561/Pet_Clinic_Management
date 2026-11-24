
package com.mycompany.petclinic.config;

import java.sql.Connection;
import java.sql.DatabaseMetaData;

public class TestConnection {

    public static void main(String[] args) {

        try (Connection conn = DatabaseConnection.getConnection()) {

            System.out.println("KET NOI THANH CONG!");

            DatabaseMetaData meta = conn.getMetaData();

            System.out.println("Database: " + conn.getCatalog());
            System.out.println("SQL Server: " + meta.getDatabaseProductVersion());

        } catch (Exception e) {
            System.out.println("KET NOI THAT BAI!");
            e.printStackTrace();
        }
    }
}
