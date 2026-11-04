package com.mycompany.petclinic.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=PetClinic;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USERNAME = "toos";
    private static final String PASSWORD = "12345678";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                URL,
                USERNAME,
                PASSWORD
        );
    }
}