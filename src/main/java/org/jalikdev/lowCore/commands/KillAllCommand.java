package org.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class KillAllCommand implements CommandExecutor, TabCompleter {

    private static final List<String> MOB_TYPES = Arrays.stream(EntityType.values())
            .filter(type -> type.isAlive()                      // Nur lebende Entities
                    && type != EntityType.PLAYER               // Spieler ausschließen
                    && type != EntityType.ARMOR_STAND          // Armorstand ausschließen
                    && type != EntityType.UNKNOWN)
            .map(type -> type.name().toLowerCase())
            .sorted()
            .collect(Collectors.toList());


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "messages.player-only");
            return true;
        }
        Player p = (Player) sender;

        if (!p.hasPermission("lowcore.killall")) {
            LowCore.sendConfigMessage(p, "messages.no-permission");
            return true;
        }

        if (args.length == 0) {
            int killed = 0;
            for (Entity e : p.getWorld().getEntities()) {
                if (isKillable(e)) {
                    e.remove();
                    killed++;
                }
            }
            LowCore.sendMessage(p, "&aKilled &e" + killed + " &amobs.");
            return true;
        }

        String typeName = args[0].toUpperCase();
        EntityType type;

        try {
            type = EntityType.valueOf(typeName);
        } catch (Exception e) {
            LowCore.sendMessage(p, "&cUnknown mob type: &e" + args[0]);
            return true;
        }

        if (args.length == 1) {
            int killed = 0;
            for (Entity e : p.getWorld().getEntities()) {
                if (e.getType() == type && isKillable(e)) {
                    e.remove();
                    killed++;
                }
            }
            LowCore.sendMessage(p, "&aKilled &e" + killed + " &aentities of type &e" + type.name().toLowerCase());
            return true;
        }

        if (args.length == 2) {
            double radius;

            try {
                radius = Double.parseDouble(args[1]);
            } catch (Exception e) {
                LowCore.sendMessage(p, "&cInvalid radius: &e" + args[1]);
                return true;
            }

            int killed = 0;

            for (Entity e : p.getNearbyEntities(radius, radius, radius)) {
                if (e.getType() == type && isKillable(e)) {
                    e.remove();
                    killed++;
                }
            }

            LowCore.sendMessage(p, "&aKilled &e" + killed + " "
                    + type.name().toLowerCase()
                    + " &awithin &e" + radius + " &ablocks.");
            return true;
        }

        LowCore.sendMessage(p, "&cUsage: &e/killall [type] [radius]");
        return true;
    }

    private boolean isKillable(Entity e) {
        return !(e instanceof Player) &&
                e.getType().isAlive() &&
                e.getType() != EntityType.ARMOR_STAND &&
                e.getType() != EntityType.UNKNOWN;
    }


    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd,
                                      @NotNull String alias, @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.killall"))
            return Collections.emptyList();

        if (args.length == 1) {
            return MOB_TYPES.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            List<String> radii = Arrays.asList("10", "25", "50", "100", "200");
            return radii.stream()
                    .filter(s -> s.startsWith(args[1]))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
