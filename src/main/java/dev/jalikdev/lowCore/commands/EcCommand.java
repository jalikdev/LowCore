package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EcCommand implements CommandExecutor, TabCompleter, Listener {

    private final LowCore plugin;
    private final Map<Inventory, UUID> offlineEcViews = new HashMap<>();

    public EcCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                LowCore.sendMessage(sender, "&cConsole must specify a player: &e/ec <player>");
                return true;
            }

            if (!sender.hasPermission("lowcore.ec")) {
                LowCore.sendConfigMessage(sender, "no-permission");
                return true;
            }

            Player player = (Player) sender;
            player.openInventory(player.getEnderChest());
            LowCore.sendMessage(sender, "&aOpened your ender chest.");
            return true;
        }

        if (!sender.hasPermission("lowcore.ec.others")) {
            LowCore.sendConfigMessage(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        Player viewer = (Player) sender;
        String targetName = args[0];

        Player target = Bukkit.getPlayerExact(targetName);
        if (target != null) {
            viewer.openInventory(target.getEnderChest());
            LowCore.sendMessage(sender, "&aOpened &e" + target.getName() + "&a's ender chest.");
            return true;
        }

        OfflinePlayer offline = Bukkit.getOfflinePlayer(targetName);
        if ((offline == null || !offline.hasPlayedBefore()) && !offline.isOnline()) {
            LowCore.sendConfigMessage(sender, "unknown-player");
            return true;
        }

        ItemStack[] data = plugin.getOfflineInventoryRepository().loadEffectiveEnderChest(offline.getUniqueId());
        if (data == null) {
            LowCore.sendMessage(sender, "&cEs gibt keine gespeicherten Offline-Enderchestdaten für &e" + offline.getName() + "&c.");
            return true;
        }

        Inventory inv = Bukkit.createInventory(viewer, 27, "§8EnderChest §7- §a" + offline.getName() + " §7(offline)");
        inv.setContents(data);

        offlineEcViews.put(inv, offline.getUniqueId());
        viewer.openInventory(inv);

        LowCore.sendMessage(sender, "&aOpened OFFLINE ender chest of &e" + offline.getName() + "&a.");
        return true;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory top = event.getView().getTopInventory();
        UUID uuid = offlineEcViews.remove(top);
        if (uuid == null) return;

        ItemStack[] contents = new ItemStack[top.getSize()];
        for (int i = 0; i < top.getSize(); i++) {
            contents[i] = top.getItem(i);
        }

        plugin.getOfflineInventoryRepository().savePendingEnderChest(uuid, contents);
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (args.length == 1 && sender.hasPermission("lowcore.ec.others")) {
            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(current)) {
                    result.add(p.getName());
                }
            }
            return result;
        }

        return Collections.emptyList();
    }
}
