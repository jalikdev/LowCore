package org.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.jalikdev.lowCore.LowCore;
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

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            LowCore.sendConfigMessage(viewer, "unknown-player");
            return true;
        }

        if (target.getUniqueId().equals(viewer.getUniqueId())) {
            LowCore.sendConfigMessage(viewer, "invsee.self");
            return true;
        }

        String title = "§8InvSee §7- §a" + target.getName();
        Inventory inv = Bukkit.createInventory(viewer, 45, title);

        syncFromTarget(target, inv);

        InvseeSession session = new InvseeSession();
        session.targetId = target.getUniqueId();
        session.inv = inv;

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
                LowCore.sendConfigMessage(viewer, "invsee.offline", "target", target.getName());
                stopSession(viewer.getUniqueId());
                return;
            }

            syncFromTarget(t, inv);
        }, 0L, 5L);

        session.taskId = taskId;
        sessions.put(viewer.getUniqueId(), session);

        viewer.openInventory(inv);

        LowCore.sendConfigMessage(
                viewer,
                "invsee.open",
                "player", viewer.getName(),
                "target", target.getName()
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
