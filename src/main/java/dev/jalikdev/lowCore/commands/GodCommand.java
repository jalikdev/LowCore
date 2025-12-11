package dev.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GodCommand implements CommandExecutor {

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

        UUID uuid = player.getUniqueId();

        if(godMode.contains(uuid)) {
            godMode.remove(uuid);
            player.setInvulnerable(false);
            LowCore.sendConfigMessage(player, "godmode.disabled");
        } else {
            godMode.add(uuid);
            player.setInvulnerable(true);
            LowCore.sendConfigMessage(player, "godmode.enabled");
        }

        return true;
    }
}
