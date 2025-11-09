package org.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GmCommand implements CommandExecutor, TabCompleter {

    private final List<String> gamemodes = Arrays.asList(
            "0", "1", "2", "3",
            "survival", "creative", "adventure", "spectator"
    );

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            LowCore.sendMessage(sender, "&cUsage: &e/gm <0|1|2|3|survival|creative|adventure|spectator> [player]");
            return true;
        }

        if (!sender.hasPermission("lowcore.gm")) {
            LowCore.sendMessage(sender, "&cYou do not have permission to use this command!");
            return true;
        }

        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                LowCore.sendMessage(sender, "&cPlayer not found!");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                LowCore.sendMessage(sender, "&cConsole must specify a player!");
                return true;
            }
            target = (Player) sender;
        }

        String input = args[0].toLowerCase();
        GameMode mode;

        switch (input) {
            case "0": case "s": case "survival":
                mode = GameMode.SURVIVAL; break;
            case "1": case "c": case "creative":
                mode = GameMode.CREATIVE; break;
            case "2": case "a": case "adventure":
                mode = GameMode.ADVENTURE; break;
            case "3": case "sp": case "spectator":
                mode = GameMode.SPECTATOR; break;
            default:
                LowCore.sendMessage(sender, "&cInvalid gamemode!");
                return true;
        }

        target.setGameMode(mode);

        String name = target.equals(sender) ? "Your" : target.getName() + "'s";
        LowCore.sendMessage(sender, "&a" + name + " gamemode &7has been set to &e" + mode.name().toLowerCase() + "&7.");

        if (!target.equals(sender)) {
            LowCore.sendMessage(target, "&7Your gamemode was changed to &e" + mode.name().toLowerCase() + " &7by &b" + sender.getName());
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (args.length == 1) {
            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();

            for (String mode : gamemodes) {
                if (mode.startsWith(current)) {
                    result.add(mode);
                }
            }
            return result;
        }

        if (args.length == 2) {
            String current = args[1].toLowerCase();
            List<String> players = new ArrayList<>();

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(current)) {
                    players.add(p.getName());
                }
            }
            return players;
        }

        return Collections.emptyList();
    }
}
