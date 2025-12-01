package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LastLogoutCommand implements CommandExecutor, TabCompleter {

    private final LowCore plugin;

    public LastLogoutCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!plugin.getConfig().getBoolean("lastlogout.enabled", true)) {
            LowCore.sendConfigMessage(sender, "lastlogout.disabled");
            return true;
        }

        if (!sender.hasPermission("lowcore.lastlogout")) {
            LowCore.sendConfigMessage(sender, "lastlogout.no-permission");
            return true;
        }

        if (args.length != 1) {
            LowCore.sendConfigMessage(sender, "lastlogout.usage");
            return true;
        }

        String targetName = args[0];

        Location loc = plugin.getLastLocationRepository().getLastLocationByName(targetName);
        if (loc == null) {
            LowCore.sendConfigMessage(sender, "lastlogout.not-found", "player", targetName);
            return true;
        }

        String world = loc.getWorld().getName();
        String x = format(loc.getX());
        String y = format(loc.getY());
        String z = format(loc.getZ());

        LowCore.sendConfigMessage(sender, "lastlogout.header", "player", targetName);
        LowCore.sendConfigMessage(sender, "lastlogout.coords",
                "world", world, "x", x, "y", y, "z", z);

        if (sender instanceof Player player) {

            String commandStr = "/tp " + x + " " + y + " " + z;

            Component clickable = Component.text("§a[Click to teleport]")
                    .hoverEvent(HoverEvent.showText(Component.text("§7Teleport to the last logout location")))
                    .clickEvent(ClickEvent.runCommand(commandStr));

            player.sendMessage(clickable);
        }

        return true;
    }

    private String format(double d) {
        return String.format("%.2f", d);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(prefix)) {
                    list.add(p.getName());
                }
            }
        }

        return list;
    }
}
