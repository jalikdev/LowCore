package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HatCommand implements CommandExecutor, TabCompleter {

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

        if (!player.hasPermission("lowcore.hat")) {
            LowCore.sendMessage(player, "&cYou do not have permission to use this command!");
            return true;
        }

        Player target = player;

        if (args.length == 1) {
            if (!player.hasPermission("lowcore.hat.others")) {
                LowCore.sendMessage(player, "&cYou do not have permission to modify other players' hats!");
                return true;
            }

            Player t = Bukkit.getPlayerExact(args[0]);
            if (t == null) {
                LowCore.sendMessage(player, "&cPlayer not found!");
                return true;
            }
            target = t;
        } else if (args.length > 1) {
            LowCore.sendMessage(player, "&cUsage: &e/hat [player]");
            return true;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            LowCore.sendMessage(player, "&cYou must hold an item in your hand!");
            return true;
        }

        PlayerInventory targetInv = target.getInventory();
        ItemStack oldHelmet = targetInv.getHelmet();

        targetInv.setHelmet(inHand);
        player.getInventory().setItemInMainHand(null);

        if (oldHelmet != null && oldHelmet.getType() != Material.AIR) {
            if (targetInv.firstEmpty() != -1) {
                targetInv.addItem(oldHelmet);
            } else {
                target.getWorld().dropItemNaturally(target.getLocation(), oldHelmet);
            }
        }

        if (target.equals(player)) {
            LowCore.sendMessage(player, "&aYou put the item on your head.");
        } else {
            LowCore.sendMessage(player, "&aYou put your item on &e" + target.getName() + "&a's head.");
            LowCore.sendMessage(target, "&e" + player.getName() + " &ahas put an item on your head.");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!(sender instanceof Player) || !sender.hasPermission("lowcore.hat.others")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            String current = args[0].toLowerCase();
            List<String> result = new ArrayList<>();

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(current)) {
                    result.add(p.getName());
                }
            }
            return result;
        }

        return Collections.emptyList();
    }
}
