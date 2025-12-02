package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import dev.jalikdev.lowCore.database.OfflineInventoryRepository;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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
            LowCore.sendConfigMessage(sender, "admin.usage");
            return true;
        }

        if (args[0].equalsIgnoreCase("offinv")) {

            if (args.length == 1) {
                LowCore.sendConfigMessage(sender, "admin.offinv-usage");
                return true;
            }

            if (args[1].equalsIgnoreCase("clear")) {
                if (args.length < 3) {
                    LowCore.sendConfigMessage(sender, "admin.offinv-usage");
                    return true;
                }

                OfflinePlayer off = Bukkit.getOfflinePlayer(args[2]);
                if ((off == null || !off.hasPlayedBefore()) && !off.isOnline()) {
                    LowCore.sendConfigMessage(sender, "unknown-player");
                    return true;
                }

                offlineRepo.deletePlayer(off.getUniqueId());
                LowCore.sendConfigMessage(sender, "admin.offinv-cleared", "target", off.getName());
                return true;
            }

            if (args[1].equalsIgnoreCase("clearall")) {
                int count = offlineRepo.deleteAll();
                LowCore.sendConfigMessage(sender, "admin.offinv-cleared-all", "count", String.valueOf(count));
                return true;
            }

            LowCore.sendConfigMessage(sender, "admin.offinv-usage");
            return true;
        }

        LowCore.sendConfigMessage(sender, "admin.usage");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.admin")) return Collections.emptyList();

        if (args.length == 1) {
            List<String> list = new ArrayList<>();
            if ("offinv".startsWith(args[0].toLowerCase())) list.add("offinv");
            return list;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("offinv")) {
            List<String> list = new ArrayList<>();
            if ("clear".startsWith(args[1].toLowerCase())) list.add("clear");
            if ("clearall".startsWith(args[1].toLowerCase())) list.add("clearall");
            return list;
        }

        if (args.length == 3
                && args[0].equalsIgnoreCase("offinv")
                && args[1].equalsIgnoreCase("clear")) {

            String input = args[2].toLowerCase();
            List<String> names = new ArrayList<>();

            for (UUID uuid : offlineRepo.getAllWithData()) {
                OfflinePlayer off = Bukkit.getOfflinePlayer(uuid);
                String name = off.getName();
                if (name != null && name.toLowerCase().startsWith(input)) names.add(name);
            }

            return names;
        }

        return Collections.emptyList();
    }
}
