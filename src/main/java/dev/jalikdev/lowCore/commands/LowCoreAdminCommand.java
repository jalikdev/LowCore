package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import dev.jalikdev.lowCore.database.OfflineInventoryRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class LowCoreAdminCommand implements CommandExecutor, TabCompleter {

    private final LowCore plugin;
    private final OfflineInventoryRepository offlineRepo;

    public LowCoreAdminCommand(LowCore plugin) {
        this.plugin = plugin;
        this.offlineRepo = plugin.getOfflineInventoryRepository();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.admin")) {
            LowCore.sendConfigMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender);
            return true;
        }

        String category = args[0].toLowerCase();

        if (category.equals("offinv")) {
            if (args.length == 1) {
                LowCore.sendConfigMessage(sender, "admin.offinv-usage");
                return true;
            }

            String action = args[1].toLowerCase();

            if (action.equals("clear")) {
                if (args.length < 3) {
                    LowCore.sendConfigMessage(sender, "admin.offinv-usage");
                    return true;
                }

                String targetName = args[2];
                OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

                if ((target == null || !target.hasPlayedBefore()) && !target.isOnline()) {
                    LowCore.sendConfigMessage(sender, "unknown-player");
                    return true;
                }

                UUID uuid = target.getUniqueId();
                offlineRepo.deletePlayer(uuid);

                LowCore.sendConfigMessage(sender, "admin.offinv-cleared", "target", target.getName());
                return true;
            }

            if (action.equals("clearall")) {
                int count = offlineRepo.deleteAll();
                LowCore.sendConfigMessage(sender, "admin.offinv-cleared-all", "count", String.valueOf(count));
                return true;
            }

            LowCore.sendConfigMessage(sender, "admin.offinv-usage");
            return true;
        }

        sendUsage(sender);
        return true;
    }

    private void sendUsage(CommandSender sender) {
        LowCore.sendConfigMessage(sender, "admin.usage");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> result = new ArrayList<>();
            String current = args[0].toLowerCase();

            if ("offinv".startsWith(current)) {
                result.add("offinv");
            }

            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("offinv")) {
            List<String> result = new ArrayList<>();
            String current = args[1].toLowerCase();

            if ("clear".startsWith(current)) result.add("clear");
            if ("clearall".startsWith(current)) result.add("clearall");

            return result;
        }

        if (args.length == 3
                && args[0].equalsIgnoreCase("offinv")
                && args[1].equalsIgnoreCase("clear")) {

            String current = args[2].toLowerCase();
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
