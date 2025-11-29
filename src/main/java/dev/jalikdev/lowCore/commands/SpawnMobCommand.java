package dev.jalikdev.lowCore.commands;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpawnMobCommand implements CommandExecutor, TabCompleter {

    private final LowCore plugin;

    public SpawnMobCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    private @Nullable Location getSpawnLocation(Player player, int maxDistance) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();

        Location lastAir = null;

        for (int i = 1; i <= maxDistance; i++) {
            Location check = eye.clone().add(direction.clone().multiply(i));
            Block block = check.getBlock();

            if (block.getType().isSolid()) {
                if (lastAir == null) {
                    return null;
                }

                Location spawn = lastAir.clone().add(0, 0, 0);
                spawn.setYaw(player.getLocation().getYaw());
                spawn.setPitch(0);
                return spawn;
            } else {
                lastAir = check.clone();
            }
        }

        if (lastAir != null) {
            Location spawn = lastAir.clone().add(0, 0, 0);
            spawn.setYaw(player.getLocation().getYaw());
            spawn.setPitch(0);
            return spawn;
        }

        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.spawnmob")) {
            LowCore.sendConfigMessage(player, "spawnmob.no-permission");
            return true;
        }

        if (args.length < 1) {
            LowCore.sendConfigMessage(player, "spawnmob.usage");
            return true;
        }

        String typeName = args[0].toUpperCase();

        EntityType type;
        try {
            type = EntityType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            LowCore.sendConfigMessage(player, "spawnmob.invalid-entity", "type", args[0]);
            return true;
        }

        if (!type.isAlive() || !type.isSpawnable()) {
            LowCore.sendConfigMessage(player, "spawnmob.invalid-entity", "type", args[0]);
            return true;
        }

        int maxAmount = plugin.getConfig().getInt("spawnmob.max-amount", 50);

        int amount = 1;
        if (args.length >= 2) {
            try {
                amount = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                String msg = plugin.formatMessage("spawnmob.invalid-amount", "max", String.valueOf(maxAmount));
                player.sendMessage(msg);
                return true;
            }
        }

        if (amount < 1 || amount > maxAmount) {
            String msg = plugin.formatMessage("spawnmob.invalid-amount", "max", String.valueOf(maxAmount));
            player.sendMessage(msg);
            return true;
        }

        List<String> restricted = plugin.getConfig().getStringList("spawnmob.restricted-mobs");
        String restrictedPermission = plugin.getConfig().getString("spawnmob.restricted-permission", "lowcore.spawnmob.restricted");

        if (restricted.stream().anyMatch(s -> s.equalsIgnoreCase(type.name()))) {
            if (!player.hasPermission(restrictedPermission)) {
                LowCore.sendConfigMessage(player, "spawnmob.restricted", "type", type.name().toLowerCase());
                return true;
            }
        }

        Location loc = getSpawnLocation(player, 30);
        if (loc == null) {
            LowCore.sendConfigMessage(player, "spawnmob.no-target");
            return true;
        }

        World world = player.getWorld();
        int spawned = 0;

        for (int i = 0; i < amount; i++) {
            LivingEntity entity = (LivingEntity) world.spawnEntity(loc, type);
            if (entity != null) {
                spawned++;
            }
        }

        if (spawned > 0) {
            String msg = plugin.formatMessage(
                    "spawnmob.success",
                    "amount", String.valueOf(spawned),
                    "type", type.name().toLowerCase()
            );
            player.sendMessage(msg);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!sender.hasPermission("lowcore.spawnmob")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> completions = new ArrayList<>();

            for (EntityType type : EntityType.values()) {
                if (!type.isAlive() || !type.isSpawnable()) continue;
                String name = type.name().toLowerCase();
                if (name.startsWith(input)) {
                    completions.add(name);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            return Arrays.asList("1", "5", "10", "20");
        }

        return new ArrayList<>();
    }
}
