package org.jalikdev.lowCore.commands;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class EnchantCommand implements CommandExecutor, TabCompleter {

    private final LowCore plugin;

    public EnchantCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    private @Nullable Enchantment resolveEnchant(String input) {
        String norm = input.toLowerCase(Locale.ROOT);

        NamespacedKey key = norm.contains(":")
                ? NamespacedKey.fromString(norm)
                : NamespacedKey.minecraft(norm);

        if (key != null) {
            Enchantment byKey = Enchantment.getByKey(key);
            if (byKey != null) {
                return byKey;
            }
        }

        for (Enchantment ench : Enchantment.values()) {
            if (ench == null || ench.getKey() == null) continue;
            String keyName = ench.getKey().getKey().toLowerCase(Locale.ROOT);
            if (keyName.equals(norm)) {
                return ench;
            }
        }

        return null;
    }

    private @Nullable ItemStack getItemInHand(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir()) {
            LowCore.sendConfigMessage(player, "enchant.no-item");
            return null;
        }
        return item;
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

        if (args.length == 0) {
            LowCore.sendConfigMessage(player, "enchant.usage");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("clear")) {
            if (!player.hasPermission("lowcore.enchant.clear")) {
                LowCore.sendConfigMessage(player, "enchant.no-permission");
                return true;
            }

            ItemStack item = getItemInHand(player);
            if (item == null) return true;

            if (item.getEnchantments().isEmpty()) {
                LowCore.sendConfigMessage(player, "enchant.none-to-clear");
                return true;
            }

            item.getEnchantments().keySet().forEach(item::removeEnchantment);
            LowCore.sendConfigMessage(player, "enchant.cleared");
            return true;
        }

        if (sub.equals("remove")) {
            if (!player.hasPermission("lowcore.enchant.remove")) {
                LowCore.sendConfigMessage(player, "enchant.no-permission");
                return true;
            }

            if (args.length < 2) {
                LowCore.sendConfigMessage(player, "enchant.usage");
                return true;
            }

            ItemStack item = getItemInHand(player);
            if (item == null) return true;

            Enchantment ench = resolveEnchant(args[1]);
            if (ench == null || !item.containsEnchantment(ench)) {
                LowCore.sendConfigMessage(player, "enchant.invalid-enchant", "enchant", args[1]);
                return true;
            }

            item.removeEnchantment(ench);

            String enchName = ench.getKey().getKey();
            LowCore.sendConfigMessage(player, "enchant.removed", "enchant", enchName);
            return true;
        }

        if (sub.equals("name") || sub.equals("rename")) {
            if (!player.hasPermission("lowcore.enchant.name")) {
                LowCore.sendConfigMessage(player, "enchant.no-permission");
                return true;
            }

            if (args.length < 2) {
                LowCore.sendConfigMessage(player, "enchant.usage");
                return true;
            }

            ItemStack item = getItemInHand(player);
            if (item == null) return true;

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) sb.append(" ");
                sb.append(args[i]);
            }

            String rawName = sb.toString();
            String colored = ChatColor.translateAlternateColorCodes('&', rawName);

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                LowCore.sendConfigMessage(player, "enchant.no-item");
                return true;
            }

            meta.setDisplayName(colored);
            item.setItemMeta(meta);

            LowCore.sendConfigMessage(player, "enchant.renamed", "name", colored);
            return true;
        }

        if (sub.equals("resetname")) {
            if (!player.hasPermission("lowcore.enchant.name")) {
                LowCore.sendConfigMessage(player, "enchant.no-permission");
                return true;
            }

            ItemStack item = getItemInHand(player);
            if (item == null) return true;

            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                LowCore.sendConfigMessage(player, "enchant.no-item");
                return true;
            }

            meta.setDisplayName(null);
            item.setItemMeta(meta);

            LowCore.sendConfigMessage(player, "enchant.resetname");
            return true;
        }

        if (!player.hasPermission("lowcore.enchant")) {
            LowCore.sendConfigMessage(player, "enchant.no-permission");
            return true;
        }

        ItemStack item = getItemInHand(player);
        if (item == null) return true;

        Enchantment ench = resolveEnchant(args[0]);
        if (ench == null) {
            LowCore.sendConfigMessage(player, "enchant.invalid-enchant", "enchant", args[0]);
            return true;
        }

        int level = 1;
        if (args.length >= 2) {
            try {
                level = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                LowCore.sendConfigMessage(player, "enchant.invalid-level");
                return true;
            }
        }

        int maxAllowed = 255;
        if (level < 1 || level > maxAllowed) {
            LowCore.sendConfigMessage(player, "enchant.invalid-level");
            return true;
        }

        boolean bypass = player.hasPermission("lowcore.enchant.bypass");
        boolean compatible = ench.canEnchantItem(item);
        String enchName = ench.getKey().getKey();

        if (!compatible && !bypass) {
            LowCore.sendConfigMessage(player, "enchant.incompatible-block", "enchant", enchName);
            return true;
        }

        try {
            if (bypass) {
                item.addUnsafeEnchantment(ench, level);
            } else {
                int vanillaMax = ench.getMaxLevel();
                if (level > vanillaMax) {
                    LowCore.sendConfigMessage(player, "enchant.invalid-level");
                    return true;
                }
                item.addEnchantment(ench, level);
            }
        } catch (IllegalArgumentException ex) {
            LowCore.sendConfigMessage(player, "enchant.invalid-enchant", "enchant", args[0]);
            return true;
        }

        LowCore.sendConfigMessage(player, "enchant.applied",
                "enchant", enchName,
                "level", String.valueOf(level));

        if (!compatible && bypass) {
            LowCore.sendConfigMessage(player, "enchant.incompatible-unsafe", "enchant", enchName);
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }

        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase(Locale.ROOT);

            if (player.hasPermission("lowcore.enchant.clear") && "clear".startsWith(input)) {
                completions.add("clear");
            }
            if (player.hasPermission("lowcore.enchant.remove") && "remove".startsWith(input)) {
                completions.add("remove");
            }
            if (player.hasPermission("lowcore.enchant.name") && "name".startsWith(input)) {
                completions.add("name");
            }
            if (player.hasPermission("lowcore.enchant.name") && "resetname".startsWith(input)) {
                completions.add("resetname");
            }

            for (Enchantment ench : Enchantment.values()) {
                if (ench == null || ench.getKey() == null) continue;
                String keyName = ench.getKey().getKey().toLowerCase(Locale.ROOT);
                if (keyName.startsWith(input)) {
                    completions.add(keyName);
                }
            }

            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType().isAir() || item.getEnchantments().isEmpty()) {
                return Collections.emptyList();
            }

            String input = args[1].toLowerCase(Locale.ROOT);
            for (Enchantment ench : item.getEnchantments().keySet()) {
                if (ench == null || ench.getKey() == null) continue;
                String keyName = ench.getKey().getKey().toLowerCase(Locale.ROOT);
                if (keyName.startsWith(input)) {
                    completions.add(keyName);
                }
            }

            return completions;
        }

        if (args.length == 2
                && !args[0].equalsIgnoreCase("clear")
                && !args[0].equalsIgnoreCase("remove")
                && !args[0].equalsIgnoreCase("name")
                && !args[0].equalsIgnoreCase("rename")
                && !args[0].equalsIgnoreCase("resetname")) {

            Enchantment ench = resolveEnchant(args[0]);
            if (ench != null) {
                int max = ench.getMaxLevel();
                for (int i = 1; i <= max; i++) {
                    completions.add(String.valueOf(i));
                }
            } else {
                completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
            }

            return completions;
        }

        if (args[0].equalsIgnoreCase("clear")) {
            return Collections.emptyList();
        }

        return Collections.emptyList();
    }
}
