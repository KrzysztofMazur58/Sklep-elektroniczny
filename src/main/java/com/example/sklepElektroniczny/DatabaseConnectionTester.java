package com.example.sklepElektroniczny;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseConnectionTester {

    @Autowired
    private DataSource dataSource;

    public void testConnection() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection != null) {
                System.out.println("Połączenie z bazą danych jest prawidłowe!");
            } else {
                System.out.println("Nie udało się połączyć z bazą danych.");
            }
        } catch (SQLException e) {
            System.out.println("Błąd podczas łączenia z bazą danych: " + e.getMessage());
        }
    }
}
