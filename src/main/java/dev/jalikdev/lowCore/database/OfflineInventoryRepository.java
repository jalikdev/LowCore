package dev.jalikdev.lowCore.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.sql.*;
import java.util.*;

public class OfflineInventoryRepository {

    public static class OfflineInventoryMeta {
        public final boolean hasInvSnapshot;
        public final boolean hasEcSnapshot;
        public final boolean hasInvPending;
        public final boolean hasEcPending;
        public final long updatedAt;

        public OfflineInventoryMeta(boolean hasInvSnapshot, boolean hasEcSnapshot, boolean hasInvPending, boolean hasEcPending, long updatedAt) {
            this.hasInvSnapshot = hasInvSnapshot;
            this.hasEcSnapshot = hasEcSnapshot;
            this.hasInvPending = hasInvPending;
            this.hasEcPending = hasEcPending;
            this.updatedAt = updatedAt;
        }
    }

    private final DatabaseManager databaseManager;

    public OfflineInventoryRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    private Connection conn() throws SQLException {
        Connection c = databaseManager.getConnection();
        if (c == null || c.isClosed()) throw new SQLException("DB connection closed");
        return c;
    }

    private String itemArrayToBase64(ItemStack[] items) {
        if (items == null) return null;
        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             BukkitObjectOutputStream out = new BukkitObjectOutputStream(byteOut)) {
            out.writeInt(items.length);
            for (ItemStack item : items) out.writeObject(item);
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
                    items[i] = null;
                }
            }
            return items;
        } catch (IOException e) {
            return null;
        }
    }

    public void saveSnapshot(UUID uuid, ItemStack[] inv, ItemStack[] ec) {
        String invB = itemArrayToBase64(inv);
        String ecB = itemArrayToBase64(ec);
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
            ps.setString(2, invB);
            ps.setString(3, ecB);
            ps.setLong(4, now);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public void savePendingInventory(UUID uuid, ItemStack[] inv) {
        String invB = itemArrayToBase64(inv);
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
            ps.setString(2, invB);
            ps.setLong(3, now);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public void savePendingEnderChest(UUID uuid, ItemStack[] ec) {
        String ecB = itemArrayToBase64(ec);
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
            ps.setString(2, ecB);
            ps.setLong(3, now);
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public ItemStack[] loadPendingInventory(UUID uuid) {
        String sql = "SELECT inv_pending FROM offline_inventories WHERE uuid = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return base64ToItemArray(rs.getString("inv_pending"));
        } catch (Exception ignored) {}
        return null;
    }

    public ItemStack[] loadPendingEnderChest(UUID uuid) {
        String sql = "SELECT ec_pending FROM offline_inventories WHERE uuid = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return base64ToItemArray(rs.getString("ec_pending"));
        } catch (Exception ignored) {}
        return null;
    }

    public void clearPending(UUID uuid) {
        try (PreparedStatement ps = conn().prepareStatement(
                "UPDATE offline_inventories SET inv_pending = NULL, ec_pending = NULL WHERE uuid = ?"
        )) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public ItemStack[] loadEffectiveInventory(UUID uuid) {
        String sql = "SELECT inv_pending, inv_snapshot FROM offline_inventories WHERE uuid = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String pending = rs.getString("inv_pending");
                if (pending != null) return base64ToItemArray(pending);
                return base64ToItemArray(rs.getString("inv_snapshot"));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public ItemStack[] loadEffectiveEnderChest(UUID uuid) {
        String sql = "SELECT ec_pending, ec_snapshot FROM offline_inventories WHERE uuid = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String pending = rs.getString("ec_pending");
                if (pending != null) return base64ToItemArray(pending);
                return base64ToItemArray(rs.getString("ec_snapshot"));
            }
        } catch (Exception ignored) {}
        return null;
    }

    public void deletePlayer(UUID uuid) {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM offline_inventories WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (Exception ignored) {}
    }

    public int deleteAll() {
        try (Statement st = conn().createStatement()) {
            return st.executeUpdate("DELETE FROM offline_inventories");
        } catch (Exception e) {
            return 0;
        }
    }

    public List<UUID> getAllWithData() {
        List<UUID> list = new ArrayList<>();
        String sql = "SELECT uuid FROM offline_inventories";

        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    list.add(UUID.fromString(rs.getString("uuid")));
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        return list;
    }

    public OfflineInventoryMeta getMeta(UUID uuid) {
        String sql = "SELECT inv_snapshot, ec_snapshot, inv_pending, ec_pending, updated_at FROM offline_inventories WHERE uuid = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                boolean hasInvSnap = rs.getString("inv_snapshot") != null;
                boolean hasEcSnap = rs.getString("ec_snapshot") != null;
                boolean hasInvPend = rs.getString("inv_pending") != null;
                boolean hasEcPend = rs.getString("ec_pending") != null;
                long updated = rs.getLong("updated_at");
                return new OfflineInventoryMeta(hasInvSnap, hasEcSnap, hasInvPend, hasEcPend, updated);
            }
        } catch (Exception ignored) {}
        return null;
    }
}
