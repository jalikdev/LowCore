package dev.jalikdev.lowCore.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.Base64;
import java.util.UUID;

public class OfflineInventoryRepository {

    private final DatabaseManager databaseManager;

    public OfflineInventoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private String itemArrayToBase64(ItemStack[] items) {
        if (items == null) return null;

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteOut)) {

            out.writeInt(items.length);
            for (ItemStack item : items) {
                out.writeObject(item);
            }
            out.flush();
            return Base64.getEncoder().encodeToString(byteOut.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private ItemStack[] base64ToItemArray(String base64) {
        if (base64 == null) return null;

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
             BukkitObjectInputStream in = new BukkitObjectInputStream(byteIn)) {

            int length = in.readInt();
            ItemStack[] items = new ItemStack[length];

            for (int i = 0; i < length; i++) {
                try {
                    items[i] = (ItemStack) in.readObject();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    items[i] = null;
                }
            }
            return items;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Connection conn() throws SQLException {
        Connection c = databaseManager.getConnection();
        if (c == null || c.isClosed()) {
            throw new SQLException("Database connection is null or closed");
        }
        return c;
    }


    public void saveSnapshot(UUID uuid, ItemStack[] invSnapshot, ItemStack[] ecSnapshot) {
        String invBase64 = itemArrayToBase64(invSnapshot);
        String ecBase64 = itemArrayToBase64(ecSnapshot);
        long now = System.currentTimeMillis();

        String sql = """
                INSERT INTO offline_inventories (uuid, inv_snapshot, ec_snapshot, updated_at)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    inv_snapshot = excluded.inv_snapshot,
                    ec_snapshot = excluded.ec_snapshot,
                    updated_at = excluded.updated_at
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, invBase64);
            ps.setString(3, ecBase64);
            ps.setLong(4, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void savePendingInventory(UUID uuid, ItemStack[] invPending) {
        String invBase64 = itemArrayToBase64(invPending);
        long now = System.currentTimeMillis();

        String sql = """
                INSERT INTO offline_inventories (uuid, inv_pending, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    inv_pending = excluded.inv_pending,
                    updated_at = excluded.updated_at
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, invBase64);
            ps.setLong(3, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePendingEnderChest(UUID uuid, ItemStack[] ecPending) {
        String ecBase64 = itemArrayToBase64(ecPending);
        long now = System.currentTimeMillis();

        String sql = """
                INSERT INTO offline_inventories (uuid, ec_pending, updated_at)
                VALUES (?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    ec_pending = excluded.ec_pending,
                    updated_at = excluded.updated_at
                """;

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, ecBase64);
            ps.setLong(3, now);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadPendingInventory(UUID uuid) {
        String sql = "SELECT inv_pending FROM offline_inventories WHERE uuid = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String base64 = rs.getString("inv_pending");
                    return base64ToItemArray(base64);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ItemStack[] loadPendingEnderChest(UUID uuid) {
        String sql = "SELECT ec_pending FROM offline_inventories WHERE uuid = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String base64 = rs.getString("ec_pending");
                    return base64ToItemArray(base64);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clearPending(UUID uuid) {
        String sql = "UPDATE offline_inventories SET inv_pending = NULL, ec_pending = NULL WHERE uuid = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ItemStack[] loadEffectiveInventory(UUID uuid) {
        String sql = "SELECT inv_pending, inv_snapshot FROM offline_inventories WHERE uuid = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String pending = rs.getString("inv_pending");
                    if (pending != null) {
                        ItemStack[] arr = base64ToItemArray(pending);
                        if (arr != null) return arr;
                    }

                    String snapshot = rs.getString("inv_snapshot");
                    if (snapshot != null) {
                        return base64ToItemArray(snapshot);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ItemStack[] loadEffectiveEnderChest(UUID uuid) {
        String sql = "SELECT ec_pending, ec_snapshot FROM offline_inventories WHERE uuid = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String pending = rs.getString("ec_pending");
                    if (pending != null) {
                        ItemStack[] arr = base64ToItemArray(pending);
                        if (arr != null) return arr;
                    }

                    String snapshot = rs.getString("ec_snapshot");
                    if (snapshot != null) {
                        return base64ToItemArray(snapshot);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deletePlayer(UUID uuid) {
        String sql = "DELETE FROM offline_inventories WHERE uuid = ?";

        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public int deleteAll() {
        String sql = "DELETE FROM offline_inventories";

        try (Statement st = conn().createStatement()) {
            return st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
