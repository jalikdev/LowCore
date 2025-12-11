package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
import org.bukkit.event.inventory.InventoryType;
import org.jetbrains.annotations.NotNull;

public class CraftCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            LowCore.sendConfigMessage(sender, "player-only");
            return true;
        }

        if (!player.hasPermission("lowcore.craft")) {
            LowCore.sendConfigMessage(player, "no-permission");
            return true;
        }

        player.openInventory(Bukkit.createInventory(player, InventoryType.WORKBENCH));

        LowCore.sendConfigMessage(player, "misc.craft-opened");
        return true;
    }
}
