package dev.jalikdev.lowCore.commands;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

public class AnvilCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "messages.player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.anvil")) {
            LowCore.sendConfigMessage(player, "messages.no-permission");
            return true;
        }

        Location loc = player.getLocation();
        player.openAnvil(loc, true);

        LowCore.sendMessage(player, "&aAnvil interface opened.");
        return true;
    }
}
