package dev.jalikdev.lowCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class NBTUtils {

    public static ItemStack[] loadInventory(UUID playerUUID) throws Exception {
        throw new UnsupportedOperationException("NBT Inventory loading not implemented.");
    }

    public static void saveInventory(UUID playerUUID, ItemStack[] items) throws Exception {
        throw new UnsupportedOperationException("NBT Inventory saving not implemented.");
    }

    public static Inventory loadEnderChest(UUID playerUUID) throws Exception {
        return Bukkit.createInventory(null, 27, "Offline Ender Chest");
    }

    public static void saveEnderChest(UUID playerUUID, Inventory ec) throws Exception {
        throw new UnsupportedOperationException("NBT Ender Chest saving not implemented.");
    }

    public static Location loadLastLocation(UUID playerUUID) {
        try {
            World defaultWorld = Bukkit.getWorlds().get(0);
            return new Location(defaultWorld, 100.5, 64.0, 50.5);
        } catch (Exception e) {
            return null;
        }
    }
}