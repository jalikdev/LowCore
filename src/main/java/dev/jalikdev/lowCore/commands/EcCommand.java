package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
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

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            LowCore.sendConfigMessage(sender, "unknown-player");
            return true;
        }

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
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
