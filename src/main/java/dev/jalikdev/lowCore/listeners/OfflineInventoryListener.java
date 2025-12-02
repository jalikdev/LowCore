package dev.jalikdev.lowCore.listeners;

import dev.jalikdev.lowCore.LowCore;
import dev.jalikdev.lowCore.database.OfflineInventoryRepository;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class OfflineInventoryListener implements Listener {

    private final LowCore plugin;
    private final OfflineInventoryRepository repository;

    public OfflineInventoryListener(LowCore plugin) {
        this.plugin = plugin;
        this.repository = plugin.getOfflineInventoryRepository();
    }

    private ItemStack[] buildInventorySnapshot(Player p) {
        PlayerInventory inv = p.getInventory();
        ItemStack[] data = new ItemStack[41];

        for (int i = 0; i < 36; i++) {
            data[i] = inv.getItem(i);
        }

        data[36] = inv.getHelmet();
        data[37] = inv.getChestplate();
        data[38] = inv.getLeggings();
        data[39] = inv.getBoots();
        data[40] = inv.getItemInOffHand();

        return data;
    }

    private void applyInventoryFromArray(Player p, ItemStack[] data) {
        if (data == null) return;

        PlayerInventory inv = p.getInventory();

        for (int i = 0; i < 36 && i < data.length; i++) {
            inv.setItem(i, data[i]);
        }

        if (data.length > 36) inv.setHelmet(data[36]);
        if (data.length > 37) inv.setChestplate(data[37]);
        if (data.length > 38) inv.setLeggings(data[38]);
        if (data.length > 39) inv.setBoots(data[39]);
        if (data.length > 40) inv.setItemInOffHand(data[40]);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        ItemStack[] invSnapshot = buildInventorySnapshot(p);
        ItemStack[] ecSnapshot = p.getEnderChest().getContents();

        repository.saveSnapshot(uuid, invSnapshot, ecSnapshot);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        UUID uuid = p.getUniqueId();

        ItemStack[] pendingInv = repository.loadPendingInventory(uuid);
        ItemStack[] pendingEc = repository.loadPendingEnderChest(uuid);

        if (pendingInv == null && pendingEc == null) {
            return;
        }

        if (pendingInv != null) {
            applyInventoryFromArray(p, pendingInv);
        }

        if (pendingEc != null) {
            p.getEnderChest().setContents(pendingEc);
        }

        repository.clearPending(uuid);
    }
}
