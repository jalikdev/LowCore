package dev.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;

public class FlyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.fly")) {
            LowCore.sendConfigMessage(player, "no-permission");
            return true;
        }

        boolean enabled = !player.getAllowFlight();
        player.setAllowFlight(enabled);

        String color = enabled ? "§a" : "§c";
        String status = enabled ? "enabled" : "disabled";

        LowCore.sendMessage(player, "Fly mode: " + color + status + "§7!");
        return true;
    }
}
