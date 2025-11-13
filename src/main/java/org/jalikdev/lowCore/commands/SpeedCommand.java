package org.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpeedCommand implements CommandExecutor, TabCompleter {

    private static final float DEFAULT_WALK_SPEED = 0.2f;
    private static final float DEFAULT_FLY_SPEED = 0.1f;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "messages.player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.speed")) {
            LowCore.sendConfigMessage(player, "messages.no-permission");
            return true;
        }

        if (args.length == 0) {
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
            player.setFlySpeed(DEFAULT_FLY_SPEED);
            LowCore.sendMessage(player, "&aReset your walk and fly speed to &estandard&a.");
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("standard")) {
            player.setWalkSpeed(DEFAULT_WALK_SPEED);
            player.setFlySpeed(DEFAULT_FLY_SPEED);
            LowCore.sendMessage(player, "&aReset your walk and fly speed to &estandard&a.");
            return true;
        }

        if (args.length == 1) {
            try {
                float value = Float.parseFloat(args[0]);
                if (value < 1 || value > 10) {
                    LowCore.sendMessage(player, "&cSpeed must be between 1 and 10.");
                    return true;
                }

                float speed = value / 10f;

                if (player.isFlying()) {
                    player.setFlySpeed(speed);
                    LowCore.sendMessage(player, "&aSet your &efly &aspeed to &e" + value + "&a.");
                } else {
                    player.setWalkSpeed(speed);
                    LowCore.sendMessage(player, "&aSet your &ewalk &aspeed to &e" + value + "&a.");
                }

            } catch (NumberFormatException e) {
                LowCore.sendMessage(player, "&cInvalid number. Use &e/speed <1-10> &cor &e/speed standard&c.");
            }
            return true;
        }

        LowCore.sendMessage(player, "&cUsage: &e/speed <1-10> &cor &e/speed standard");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {

        if (!(sender instanceof Player) || !sender.hasPermission("lowcore.speed")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("standard");
            for (int i = 1; i <= 10; i++) {
                suggestions.add(String.valueOf(i));
            }

            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();
            for (String s : suggestions) {
                if (s.toLowerCase().startsWith(current)) {
                    result.add(s);
                }
            }
            return result;
        }

        return Collections.emptyList();
    }
}
