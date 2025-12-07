package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class NightVisionCommand implements CommandExecutor, Listener {

    private final LowCore plugin;
    private final Set<UUID> enabled = new HashSet<>();

    public NightVisionCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendMessage(sender, "&cOnly players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.nightvision")) {
            LowCore.sendMessage(player, "&cYou don't have permission to use this.");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (enabled.contains(uuid)) {
            enabled.remove(uuid);
            player.removePotionEffect(PotionEffectType.NIGHT_VISION);
            LowCore.sendMessage(player, "&7Fullbright &cdisabled&7.");
        } else {
            PotionEffect effect = new PotionEffect(
                    PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    false
            );
            player.addPotionEffect(effect);
            enabled.add(uuid);
            LowCore.sendMessage(player, "&7Fullbright &aenabled&7.");
        }

        return true;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (enabled.contains(player.getUniqueId())) {
            PotionEffect effect = new PotionEffect(
                    PotionEffectType.NIGHT_VISION,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false,
                    false
            );
            player.addPotionEffect(effect);
        }
    }
}
