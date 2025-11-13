package org.jalikdev.lowCore.listeners;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jalikdev.lowCore.LowCore;

public class JoinQuitListener implements Listener {

    private final LowCore plugin;

    public JoinQuitListener(LowCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("join-quit-messages.enabled", true)) {
            String raw = plugin.getConfig().getString("join-quit-messages.join", "&a+ &7%player%");
            raw = raw.replace("%player%", player.getName());
            event.setJoinMessage(ChatColor.translateAlternateColorCodes('&', raw));
        }

        if (plugin.getConfig().getBoolean("update-checker.notify-players-with-permission", true)
                && plugin.isUpdateAvailable()
                && player.hasPermission("lowcore.update")) {

            String latest = plugin.getLatestVersion();
            String current = plugin.getDescription().getVersion();

            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&8[&aLowCore&8] &7A new &aupdate &7is available!"));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7Current: &c" + current + " &7→ Latest: &a" + latest));
            player.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    "&7Download: &ahttps://github.com/jalikdev/LowCore/releases"));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("join-quit-messages.enabled", true)) return;

        Player player = event.getPlayer();
        String raw = plugin.getConfig().getString("join-quit-messages.quit", "&c- &7%player%");
        raw = raw.replace("%player%", player.getName());
        event.setQuitMessage(ChatColor.translateAlternateColorCodes('&', raw));
    }
}
