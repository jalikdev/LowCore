package org.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jalikdev.lowCore.LowCore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LowcoreCommand implements CommandExecutor, TabCompleter {

    private final List<String> subcommands = Arrays.asList("reload", "info", "help");
    private final LowCore plugin;

    public LowcoreCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§aLowCore Plugin §7- §e/help §7for commands");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if(!sender.hasPermission("lowcore.reload")) {
                    LowCore.sendConfigMessage(sender, "no-permission");
                    return true;
                }
                plugin.reloadLowCoreConfig(sender);
                break;

            case "info":
                sender.sendMessage("&Running &a Lowcore &7by &ajalikdev&7.");
                break;

            case "help":
                sender.sendMessage("&aAvailable commands: reload, info, help");
                break;

            default:
                sender.sendMessage("&cUnknown subcommand. Use /lowcore help for a list of commands.");
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

            for (String sub : subcommands) {
                if (sub.startsWith(current)) {
                    result.add(sub);
                }
            }
            return result;
        }
        return Collections.emptyList();
    }
}

