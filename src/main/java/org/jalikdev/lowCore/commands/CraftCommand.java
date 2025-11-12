package org.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

public class CraftCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.craft")) {
            LowCore.sendConfigMessage(player, "no-permission");
            return true;
        }

        player.openWorkbench(null, true);

        LowCore.sendConfigMessage(player, "craft-opened");
        return true;
    }
}
