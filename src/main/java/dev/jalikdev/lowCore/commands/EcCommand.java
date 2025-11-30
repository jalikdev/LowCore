package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import dev.jalikdev.lowCore.LowCore;
import dev.jalikdev.lowCore.utils.PlayerUtils;
import dev.jalikdev.lowCore.utils.NBTUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EcCommand implements CommandExecutor, TabCompleter {

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

        OfflinePlayer targetPlayer = PlayerUtils.getOfflinePlayerRobust(args[0]);

        if (targetPlayer == null || (!targetPlayer.hasPlayedBefore() && targetPlayer.getPlayer() == null)) {
            LowCore.sendConfigMessage(sender, "unknown-player");
            return true;
        }

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        Player viewer = (Player) sender;
        Player target = targetPlayer.getPlayer();

        Inventory ecInv;
        String targetName = targetPlayer.getName() != null ? targetPlayer.getName() : targetPlayer.getUniqueId().toString().substring(0, 8);


        if (target != null && target.isOnline()) {
            ecInv = target.getEnderChest();
            LowCore.sendMessage(sender, "&aOpened &e" + targetName + "&a's ender chest. &7(Live)");
        } else {
            try {
                ecInv = NBTUtils.loadEnderChest(targetPlayer.getUniqueId());

                Location lastLoc = NBTUtils.loadLastLocation(targetPlayer.getUniqueId());
                if (lastLoc != null) {
                    LowCore.sendMessage(viewer, "§7Last Pos: §f"
                            + lastLoc.getWorld().getName() + " §7(§a"
                            + (int) lastLoc.getX() + ", "
                            + (int) lastLoc.getY() + ", "
                            + (int) lastLoc.getZ() + "§7)");
                }

            } catch (Exception e) {
                LowCore.sendMessage(viewer, "§cError loading Ender Chest: " + e.getMessage());
                return true;
            }

            LowCore.sendMessage(sender, "&aOpened &e" + targetName + "&a's ender chest. &7(Offline)");
        }

        viewer.openInventory(ecInv);
        return true;
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