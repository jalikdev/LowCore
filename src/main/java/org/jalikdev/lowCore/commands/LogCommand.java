package org.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogCommand implements CommandExecutor, Listener, TabCompleter {

    private final LowCore plugin;
    private final Deque<String> recent = new ArrayDeque<>();
    private final int maxEntries = 200;
    private final File logFile;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogCommand(LowCore plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "admin-actions.log");
    }


    private void log(Player player, String fullCommand) {
        String ts = LocalDateTime.now().format(formatter);
        String line = "[" + ts + "] " + player.getName() + " executed: " + fullCommand;

        synchronized (recent) {
            recent.addLast(line);
            if (recent.size() > maxEntries) {
                recent.removeFirst();
            }
        }

        try {
            if (!logFile.getParentFile().exists()) {
                logFile.getParentFile().mkdirs();
            }
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(logFile, true))) {
                bw.write(line);
                bw.newLine();
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to write admin log: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();

        if (!msg.startsWith("/")) return;

        String[] parts = msg.substring(1).split(" ");
        if (parts.length == 0) return;

        String label = parts[0];

        org.bukkit.command.PluginCommand pc = plugin.getCommand(label);
        if (pc == null || pc.getPlugin() != plugin) {
            return;
        }

        log(player, msg);
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "messages.player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.log")) {
            LowCore.sendConfigMessage(player, "messages.no-permission");
            return true;
        }

        int amount = 10;

        if (args.length >= 1) {
            try {
                amount = Integer.parseInt(args[0]);
                if (amount <= 0) amount = 10;
                if (amount > 50) amount = 50;
            } catch (NumberFormatException e) {
                LowCore.sendMessage(player, "&cUsage: &e/log &7[amount]");
                return true;
            }
        }

        List<String> lines;
        synchronized (recent) {
            lines = new ArrayList<>(recent);
        }

        if (lines.isEmpty()) {
            LowCore.sendMessage(player, "&7No admin actions have been logged yet.");
            return true;
        }

        LowCore.sendMessage(player, "&8&m-------------------------------");
        LowCore.sendMessage(player, "&aLast &e" + Math.min(amount, lines.size()) + "&a admin actions:");
        int start = Math.max(0, lines.size() - amount);
        for (int i = start; i < lines.size(); i++) {
            player.sendMessage(lines.get(i));
        }
        LowCore.sendMessage(player, "&8&m-------------------------------");

        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.log")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = Arrays.asList("10", "20", "30", "40", "50");
            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();

            for (String s : suggestions) {
                if (s.startsWith(current)) {
                    result.add(s);
                }
            }
            return result;
        }

        return Collections.emptyList();
    }
}
