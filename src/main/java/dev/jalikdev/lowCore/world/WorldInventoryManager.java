package dev.jalikdev.lowCore.world;

import dev.jalikdev.lowCore.LowCore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

public class WorldInventoryManager {

    private final LowCore plugin;

    public WorldInventoryManager(LowCore plugin) {
        this.plugin = plugin;
    }

    public void savePlayerInventory(Player player, String groupKey) {
        UUID uuid = player.getUniqueId();
        String basePath = "world.inventories." + uuid + "." + groupKey;

        try {
            String invData = itemStackArrayToBase64(player.getInventory().getContents());
            String armorData = itemStackArrayToBase64(player.getInventory().getArmorContents());
            String ecData = itemStackArrayToBase64(player.getEnderChest().getContents());

            plugin.getConfig().set(basePath + ".inventory", invData);
            plugin.getConfig().set(basePath + ".armor", armorData);
            plugin.getConfig().set(basePath + ".enderchest", ecData);
            plugin.saveConfig();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save inventory for " + player.getName() + " in group " + groupKey);
            e.printStackTrace();
        }
    }

    public void loadPlayerInventory(Player player, String groupKey) {
        UUID uuid = player.getUniqueId();
        String basePath = "world.inventories." + uuid + "." + groupKey;

        String invData = plugin.getConfig().getString(basePath + ".inventory", null);
        String armorData = plugin.getConfig().getString(basePath + ".armor", null);
        String ecData = plugin.getConfig().getString(basePath + ".enderchest", null);

        if (invData == null || armorData == null || ecData == null) {
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.getEnderChest().clear();
            return;
        }

        try {
            ItemStack[] inv = itemStackArrayFromBase64(invData);
            ItemStack[] armor = itemStackArrayFromBase64(armorData);
            ItemStack[] ec = itemStackArrayFromBase64(ecData);

            player.getInventory().setContents(inv);
            player.getInventory().setArmorContents(armor);
            player.getEnderChest().setContents(ec);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load inventory for " + player.getName() + " in group " + groupKey);
            e.printStackTrace();
        }
    }

    private String itemStackArrayToBase64(ItemStack[] items) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

        dataOutput.writeInt(items.length);
        for (ItemStack item : items) {
            dataOutput.writeObject(item);
        }
        dataOutput.close();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    private ItemStack[] itemStackArrayFromBase64(String data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        int length = dataInput.readInt();
        ItemStack[] items = new ItemStack[length];

        for (int i = 0; i < length; i++) {
            items[i] = (ItemStack) dataInput.readObject();
        }
        dataInput.close();
        return items;
    }
}
