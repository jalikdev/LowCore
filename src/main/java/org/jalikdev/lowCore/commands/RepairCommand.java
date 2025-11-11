package org.jalikdev.lowCore.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

public class RepairCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "messages.player-only");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("lowcore.repair")) {
            LowCore.sendConfigMessage(player, "messages.no-permission");
            return true;
        }

        PlayerInventory inv = player.getInventory();

        if (args.length == 0) {
            ItemStack item = inv.getItemInMainHand();

            if (!repairItem(item)) {
                LowCore.sendMessage(player, "&cYou must hold a repairable item in your hand.");
                return true;
            }

            LowCore.sendMessage(player, "&aYour held item has been repaired.");
            return true;
        }

        if (args[0].equalsIgnoreCase("all")) {
            int repaired = 0;

            for (ItemStack item : inv.getContents()) {
                if (repairItem(item)) {
                    repaired++;
                }
            }

            LowCore.sendMessage(player, "&aRepaired &e" + repaired + " &aitems in your inventory.");
            return true;
        }

        LowCore.sendMessage(player, "&cUsage: /repair [all]");
        return true;
    }

    private boolean repairItem(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            return false;
        }

        Damageable damageable = (Damageable) meta;
        if (damageable.getDamage() <= 0) {
            return false;
        }

        damageable.setDamage(0);
        item.setItemMeta(meta);
        return true;
    }
}

