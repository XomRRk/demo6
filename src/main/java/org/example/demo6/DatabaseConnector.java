package org.example.demo6;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {


    // Замени на свои данные!
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/micro_switch_db"; // Укажи имя своей БД
    private static final String USER = "postgres"; // Укажи своего пользователя PostgreSQL
    private static final String PASS = "3066"; // Укажи свой пароль

    public static Connection getConnection() throws SQLException {
        // Блок с Class.forName("org.postgresql.Driver"); был здесь и теперь удален.
        // DriverManager должен автоматически найти драйвер, если он правильно
        // подключен через pom.xml и объявлен в module-info.java (для модульных проектов).
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}