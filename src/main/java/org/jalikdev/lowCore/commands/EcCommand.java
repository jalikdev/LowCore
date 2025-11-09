package org.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jalikdev.lowCore.LowCore;
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
            // eigene Enderchest
            if (!(sender instanceof Player)) {
                LowCore.sendMessage(sender, "&cConsole must specify a player: &e/ec <player>");
                return true;
            }

            if (!sender.hasPermission("lowcore.ec")) {
                LowCore.sendMessage(sender, "&cYou do not have permission to use this command!");
                return true;
            }

            Player player = (Player) sender;
            player.openInventory(player.getEnderChest());
            LowCore.sendMessage(sender, "&aOpened your ender chest.");
            return true;
        }

        if (!sender.hasPermission("lowcore.ec.others")) {
            LowCore.sendMessage(sender, "&cYou do not have permission to open other players' ender chests!");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            LowCore.sendMessage(sender, "&cPlayer not found!");
            return true;
        }

        if (!(sender instanceof Player)) {
            LowCore.sendMessage(sender, "&cOnly players can view ender chests directly.");
            return true;
        }

        Player viewer = (Player) sender;
        viewer.openInventory(target.getEnderChest());
        LowCore.sendMessage(sender, "&aOpened &e" + target.getName() + "&a's ender chest.");
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
