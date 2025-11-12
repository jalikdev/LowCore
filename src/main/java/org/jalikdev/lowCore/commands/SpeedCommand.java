package org.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

public class SpeedCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.speed")) {
            LowCore.sendConfigMessage(player, "no-permission");
            return true;
        }

        if (args.length == 0) {
            LowCore.sendMessage(player, "&cUsage: /speed <1-10>");
            return true;
        }

        try {
            float value = Float.parseFloat(args[0]);
            if (value < 1 || value > 10) {
                LowCore.sendMessage(player, "&cSpeed must be between 1 and 10.");
                return true;
            }

            float speed = value / 10f;
            if (player.isFlying()) player.setFlySpeed(speed);
            else player.setWalkSpeed(speed);

            LowCore.sendMessage(player, "&aSet your " +
                    (player.isFlying() ? "fly" : "walk") +
                    " speed to &e" + value + "&a.");
        } catch (NumberFormatException e) {
            LowCore.sendMessage(player, "&cInvalid number. Use /speed <1-10>.");
        }

        return true;
    }
}
