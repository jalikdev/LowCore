package dev.jalikdev.lowCore.database;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private Connection connection;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() throws SQLException {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        File dbFile = new File(plugin.getDataFolder(), "data.db");
        String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        connection = DriverManager.getConnection(url);
        createTables();
    }

    private void createTables() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS last_locations (" +
                "uuid TEXT PRIMARY KEY," +
                "name TEXT," +
                "world TEXT," +
                "x DOUBLE," +
                "y DOUBLE," +
                "z DOUBLE," +
                "yaw FLOAT," +
                "pitch FLOAT," +
                "last_seen TIMESTAMP" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {}
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
