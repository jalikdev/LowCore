package dev.jalikdev.lowCore.database;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class LastLocationRepository {

    private final DatabaseManager db;

    public LastLocationRepository(DatabaseManager db) {
        this.db = db;
    }

    public void saveLogoutLocation(Player player) {
        Location loc = player.getLocation();
        Connection con = db.getConnection();

        String sql = "INSERT OR REPLACE INTO last_locations " +
                "(uuid, name, world, x, y, z, yaw, pitch, last_seen) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, player.getName());
            ps.setString(3, loc.getWorld().getName());
            ps.setDouble(4, loc.getX());
            ps.setDouble(5, loc.getY());
            ps.setDouble(6, loc.getZ());
            ps.setFloat(7, loc.getYaw());
            ps.setFloat(8, loc.getPitch());
            ps.setTimestamp(9, new Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
