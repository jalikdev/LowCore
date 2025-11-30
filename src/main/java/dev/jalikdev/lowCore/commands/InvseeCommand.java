package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.command.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import dev.jalikdev.lowCore.LowCore;
import dev.jalikdev.lowCore.utils.PlayerUtils;
import dev.jalikdev.lowCore.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InvseeCommand implements CommandExecutor, TabCompleter, Listener {

    private final LowCore plugin;
    private final Map<UUID, InvseeSession> sessions = new HashMap<>();

    public InvseeCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    private static class InvseeSession {
        UUID targetId;
        Inventory inv;
        int taskId;
    }

    private void syncToTarget(Inventory inv, Player target) {
        PlayerInventory tInv = target.getInventory();

        for (int i = 0; i < 36; i++) {
            tInv.setItem(i, inv.getItem(i));
        }

        tInv.setHelmet(inv.getItem(36));
        tInv.setChestplate(inv.getItem(37));
        tInv.setLeggings(inv.getItem(38));
        tInv.setBoots(inv.getItem(39));
        tInv.setItemInOffHand(inv.getItem(40));

        target.updateInventory();
    }

    private void syncFromTarget(Player target, Inventory inv) {
        PlayerInventory tInv = target.getInventory();

        for (int i = 0; i < 36; i++) {
            inv.setItem(i, tInv.getItem(i));
        }

        inv.setItem(36, tInv.getHelmet());
        inv.setItem(37, tInv.getChestplate());
        inv.setItem(38, tInv.getLeggings());
        inv.setItem(39, tInv.getBoots());
        inv.setItem(40, tInv.getItemInOffHand());
    }

    private void syncFromOffline(ItemStack[] offlineItems, Inventory inv) {
        if (offlineItems == null || offlineItems.length < 41) return;

        for (int i = 0; i < 36; i++) {
            inv.setItem(i, offlineItems[i]);
        }

        inv.setItem(36, offlineItems[36]);
        inv.setItem(37, offlineItems[37]);
        inv.setItem(38, offlineItems[38]);
        inv.setItem(39, offlineItems[39]);
        inv.setItem(40, offlineItems[40]);
    }

    private ItemStack[] syncToOffline(Inventory inv) {
        ItemStack[] itemsToSave = new ItemStack[41];
        for (int i = 0; i < 36; i++) {
            itemsToSave[i] = inv.getItem(i);
        }
        itemsToSave[36] = inv.getItem(36);
        itemsToSave[37] = inv.getItem(37);
        itemsToSave[38] = inv.getItem(38);
        itemsToSave[39] = inv.getItem(39);
        itemsToSave[40] = inv.getItem(40);
        return itemsToSave;
    }

    private void stopSession(UUID viewerId) {
        InvseeSession s = sessions.remove(viewerId);
        if (s != null) {
            Bukkit.getScheduler().cancelTask(s.taskId);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        Player viewer = (Player) sender;

        if (!viewer.hasPermission("lowcore.invsee")) {
            LowCore.sendConfigMessage(viewer, "no-permission");
            return true;
        }

        if (args.length != 1) {
            LowCore.sendConfigMessage(viewer, "invsee.usage");
            return true;
        }

        OfflinePlayer targetPlayer = PlayerUtils.getOfflinePlayerRobust(args[0]);

        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && targetPlayer.getPlayer() == null)) {
            LowCore.sendConfigMessage(viewer, "unknown-player");
            return true;
        }

        Player target = targetPlayer.getPlayer();

        if (targetPlayer.getUniqueId().equals(viewer.getUniqueId())) {
            LowCore.sendConfigMessage(viewer, "invsee.self");
            return true;
        }

        String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : targetPlayer.getUniqueId().toString().substring(0, 8);
        String title = "§8InvSee §7- §a" + targetName;
        Inventory inv = Bukkit.createInventory(viewer, 45, title);

        InvseeSession session = new InvseeSession();
        session.targetId = targetPlayer.getUniqueId();
        session.inv = inv;
        session.taskId = -1;

        if (target != null && target.isOnline()) {
            syncFromTarget(target, inv);

            int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                if (!viewer.isOnline()
                        || viewer.getOpenInventory() == null
                        || !viewer.getOpenInventory().getTopInventory().equals(inv)) {
                    stopSession(viewer.getUniqueId());
                    return;
                }

                Player t = Bukkit.getPlayer(session.targetId);
                if (t == null || !t.isOnline()) {
                    viewer.closeInventory();
                    LowCore.sendConfigMessage(viewer, "invsee.offline", "target", targetName);
                    stopSession(viewer.getUniqueId());
                    return;
                }

                syncFromTarget(t, inv);
            }, 0L, 5L);

            session.taskId = taskId;
        } else {
            try {
                ItemStack[] offlineItems = NBTUtils.loadInventory(targetPlayer.getUniqueId());
                syncFromOffline(offlineItems, inv);

                Location lastLoc = NBTUtils.loadLastLocation(targetPlayer.getUniqueId());
                if (lastLoc != null) {
                    LowCore.sendMessage(viewer, "§7Last Pos: §f"
                            + lastLoc.getWorld().getName() + " §7(§a"
                            + (int) lastLoc.getX() + ", "
                            + (int) lastLoc.getY() + ", "
                            + (int) lastLoc.getZ() + "§7)");
                } else {
                    LowCore.sendMessage(viewer, "§7Last Pos: §cUnknown.");
                }
            } catch (Exception e) {
                LowCore.sendMessage(viewer, "§cError loading " + targetName + "'s inventory: " + e.getMessage());
                return true;
            }
        }

        sessions.put(viewer.getUniqueId(), session);

        viewer.openInventory(inv);

        LowCore.sendConfigMessage(
                viewer,
                "invsee.open",
                "player", viewer.getName(),
                "target", targetName
        );

        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) event.getWhoClicked();
        InvseeSession session = sessions.get(viewer.getUniqueId());
        if (session == null) return;

        if (!event.getView().getTopInventory().equals(session.inv)) return;

        if (!viewer.hasPermission("lowcore.invsee.edit")) {
            event.setCancelled(true);
            return;
        }

        if (session.taskId == -1) return;

        Inventory clicked = event.getClickedInventory();
        if (clicked == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = Bukkit.getPlayer(session.targetId);
            if (target == null || !target.isOnline()) {
                viewer.closeInventory();
                stopSession(viewer.getUniqueId());
                return;
            }
            syncToTarget(session.inv, target);
        });
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player viewer = (Player) event.getWhoClicked();
        InvseeSession session = sessions.get(viewer.getUniqueId());
        if (session == null) return;

        if (!event.getView().getTopInventory().equals(session.inv)) return;

        if (!viewer.hasPermission("lowcore.invsee.edit")) {
            event.setCancelled(true);
            return;
        }

        if (session.taskId == -1) return;

        Bukkit.getScheduler().runTask(plugin, () -> {
            Player target = Bukkit.getPlayer(session.targetId);
            if (target == null || !target.isOnline()) {
                viewer.closeInventory();
                stopSession(viewer.getUniqueId());
                return;
            }
            syncToTarget(session.inv, target);
        });
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player viewer = (Player) event.getPlayer();
        InvseeSession session = sessions.get(viewer.getUniqueId());
        if (session == null) return;

        if (event.getView().getTopInventory().equals(session.inv)) {

            if (session.taskId == -1) {

                if (viewer.hasPermission("lowcore.invsee.edit")) {
                    try {
                        ItemStack[] itemsToSave = syncToOffline(session.inv);
                        NBTUtils.saveInventory(session.targetId, itemsToSave);
                        LowCore.sendMessage(viewer, "§aInventory saved successfully.");
                    } catch (Exception e) {
                        LowCore.sendMessage(viewer, "§cError saving offline data: " + e.getMessage());
                    }
                } else {
                    LowCore.sendMessage(viewer, "§cNo permission to save changes.");
                }
            }

            stopSession(viewer.getUniqueId());
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.invsee")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();

            Player self = (sender instanceof Player) ? (Player) sender : null;

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (self != null && p.getUniqueId().equals(self.getUniqueId())) continue;

                if (p.getName().toLowerCase().startsWith(current)) {
                    result.add(p.getName());
                }
            }
            return result;
        }

        return Collections.emptyList();
    }
}