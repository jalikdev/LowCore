package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor, Listener {

    private final LowCore plugin;
    private final Set<UUID> vanished = new HashSet<>();

    public VanishCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "messages.player-only");
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.vanish")) {
            LowCore.sendConfigMessage(player, "messages.no-permission");
            return true;
        }

        if (vanished.contains(player.getUniqueId())) {
            vanished.remove(player.getUniqueId());

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player)) p.showPlayer(plugin, player);
            }

            try {
                player.setSilent(false);
                player.setCollidable(true);
                player.setCanPickupItems(true);
            } catch (Throwable ignored) {}

            player.sendMessage(colorize(plugin.getPrefix() + getCfg("vanish.disabled", "&eYou are now visible.")));

            Bukkit.broadcastMessage (replacePlayer(colorize(getCfg("vanish.messages.fake-join", "+ %player%")), player));
        } else {
            vanished.add(player.getUniqueId());

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.equals(player)) p.hidePlayer(plugin, player);
            }

            try {
                player.setSilent(true);
                player.setCollidable(false);
                player.setCanPickupItems(false);
            } catch (Throwable ignored) {}

            player.sendMessage(colorize(plugin.getPrefix() + getCfg("vanish.enabled", "&aYou are now vanished.")));

            Bukkit.broadcastMessage(replacePlayer(colorize(getCfg("vanish.messages.fake-quit", "- %player%")), player));
        }

        return true;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiner = event.getPlayer();
        if (vanished.isEmpty()) return;

        for (UUID id : vanished) {
            Player v = Bukkit.getPlayer(id);
            if (v != null && v.isOnline()) {
                joiner.hidePlayer(plugin, v);
            }
        }
    }


    private String getCfg(String path, String def) {
        String raw = plugin.getConfig().getString(path, def);
        return raw != null ? raw : def;
    }

    private String replacePlayer(String msg, Player p) {
        return msg.replace("%player%", p.getName());
    }

    private String colorize(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
