package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.utils.CompletionUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GodCommand implements CommandExecutor, TabCompleter {

    private final Set<UUID> godMode = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)){
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        if (!(sender.hasPermission("lowcore.god"))) {
            LowCore.sendConfigMessage(sender, "no-permission");
            return true;
        }

        Player target = player;

        if (args.length == 1) {
            if (!sender.hasPermission("lowcore.god.others")) {
                LowCore.sendConfigMessage(sender, "godmode.permission-others");
                return true;
            }

            Player t = Bukkit.getPlayerExact(args[0]);
            if (t == null) {
                LowCore.sendMessage(player, "&cPlayer not found!");
                return true;
            }
            target = t;
        } else if (args.length > 1) {
        LowCore.sendMessage(player, "&cUsage: &e/god [player]");
        return true;
    }

        UUID uuid = target.getUniqueId();

        if(godMode.contains(uuid)) {
            godMode.remove(uuid);
            target.setInvulnerable(false);
            LowCore.sendConfigMessage(target, "godmode.disabled");
        } else {
            godMode.add(uuid);
            target.setInvulnerable(true);
            LowCore.sendConfigMessage(target, "godmode.enabled");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (args.length == 1 && sender.hasPermission("lowcore.god.others")) {
            return CompletionUtil.onlinePlayers(args[0]);
        }

        return Collections.emptyList();
    }
}
