package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ItemEditCommand implements CommandExecutor, TabCompleter {

    private final LowCore plugin;

    public ItemEditCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!plugin.getConfig().getBoolean("itemedit.enabled", true)) {
            LowCore.sendConfigMessage(sender, "itemedit.messages.disabled");
            return true;
        }

        if (!sender.hasPermission(plugin.getConfig().getString("itemedit.permission", "lowcore.itemedit"))) {
            LowCore.sendConfigMessage(sender, "itemedit.messages.no-permission");
            return true;
        }

        if (!(sender instanceof Player)) {
            LowCore.sendConfigMessage(sender, "itemedit.messages.player-only");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || item.getType() == Material.AIR) {
            LowCore.sendConfigMessage(player, "itemedit.messages.no-item");
            return true;
        }

        if (args.length == 0) {
            LowCore.sendConfigMessage(player, "itemedit.messages.usage");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            LowCore.sendConfigMessage(player, "itemedit.messages.no-itemmeta");
            return true;
        }

        switch (sub) {

            case "name": {
                if (args.length < 2) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.name-usage");
                    return true;
                }
                String nameText = color(join(args, 1));
                meta.setDisplayName(nameText);
                item.setItemMeta(meta);
                LowCore.sendConfigMessage(player, "itemedit.messages.name-updated");
                return true;
            }

            case "resetname": {
                meta.setDisplayName(null);
                item.setItemMeta(meta);
                LowCore.sendConfigMessage(player, "itemedit.messages.name-reset");
                return true;
            }

            case "lore": {
                if (args.length < 2) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.lore-usage");
                    return true;
                }

                String loreSub = args[1].toLowerCase(Locale.ROOT);
                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();

                if (loreSub.equals("add")) {
                    if (args.length < 3) {
                        LowCore.sendConfigMessage(player, "itemedit.messages.lore-add-usage");
                        return true;
                    }
                    String line = color(join(args, 2));
                    lore.add(line);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    LowCore.sendConfigMessage(player, "itemedit.messages.lore-added");
                    return true;
                }

                if (loreSub.equals("set")) {
                    if (args.length < 4) {
                        LowCore.sendConfigMessage(player, "itemedit.messages.lore-set-usage");
                        return true;
                    }
                    int index;
                    try {
                        index = Integer.parseInt(args[2]) - 1;
                    } catch (NumberFormatException e) {
                        LowCore.sendConfigMessage(player, "itemedit.messages.invalid-number");
                        return true;
                    }
                    if (index < 0) {
                        LowCore.sendConfigMessage(player, "itemedit.messages.invalid-number");
                        return true;
                    }
                    String line = color(join(args, 3));
                    while (lore.size() <= index) {
                        lore.add("");
                    }
                    lore.set(index, line);
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                    LowCore.sendConfigMessage(player, "itemedit.messages.lore-set");
                    return true;
                }

                if (loreSub.equals("clear")) {
                    meta.setLore(null);
                    item.setItemMeta(meta);
                    LowCore.sendConfigMessage(player, "itemedit.messages.lore-cleared");
                    return true;
                }

                LowCore.sendConfigMessage(player, "itemedit.messages.invalid-subcommand");
                return true;
            }

            // Simple lore sign for normal items (no extra meta line)
            case "sign": {
                if (item.getType() == Material.WRITABLE_BOOK || item.getType() == Material.WRITTEN_BOOK) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.sign-book-hint");
                    return true;
                }

                String baseItemName = meta.hasDisplayName()
                        ? ChatColor.stripColor(meta.getDisplayName())
                        : prettifyMaterial(item.getType());

                String signer = args.length >= 2 ? join(args, 1) : player.getName();

                String format = plugin.getConfig().getString(
                        "itemedit.sign-format",
                        "&8Signed by &a%signer%"
                );

                String line = format
                        .replace("%signer%", signer)
                        .replace("%item%", baseItemName);

                line = color(line);

                List<String> lore = meta.getLore();
                if (lore == null) lore = new ArrayList<>();

                String rawPrefix = plugin.getConfig().getString("itemedit.sign-prefix", "&8Signed by");
                String coloredPrefix = ChatColor.stripColor(color(rawPrefix));

                boolean updated = false;
                for (int i = 0; i < lore.size(); i++) {
                    String existing = ChatColor.stripColor(lore.get(i));
                    if (existing.startsWith(coloredPrefix)) {
                        lore.set(i, line);
                        updated = true;
                        break;
                    }
                }

                if (!updated) {
                    lore.add(line);
                }

                meta.setLore(lore);
                item.setItemMeta(meta);

                LowCore.sendConfigMessage(player, "itemedit.messages.sign-updated",
                        "signer", signer
                );
                return true;
            }

            // Book-specific subcommands: /itemedit book sign <name>
            case "book": {
                if (args.length < 2) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.book-usage");
                    return true;
                }

                String bookSub = args[1].toLowerCase(Locale.ROOT);

                if (bookSub.equals("sign")) {
                    return handleBookSign(player, item, args);
                }

                LowCore.sendConfigMessage(player, "itemedit.messages.invalid-subcommand");
                return true;
            }

            case "unlockbook": {
                if (item.getType() != Material.WRITTEN_BOOK) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.unlockbook-not-written");
                    return true;
                }

                if (!(meta instanceof BookMeta)) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.unlockbook-failed");
                    return true;
                }

                BookMeta writtenMeta = (BookMeta) meta;

                item.setType(Material.WRITABLE_BOOK);
                BookMeta editableMeta = (BookMeta) item.getItemMeta();
                if (editableMeta == null) {
                    LowCore.sendConfigMessage(player, "itemedit.messages.unlockbook-failed");
                    return true;
                }

                editableMeta.setPages(writtenMeta.getPages());

                boolean copyTitle = plugin.getConfig().getBoolean("itemedit.book-copy-title", true);
                if (copyTitle && writtenMeta.hasTitle()) {
                    editableMeta.setDisplayName(color("&f" + writtenMeta.getTitle()));
                }

                item.setItemMeta(editableMeta);

                LowCore.sendConfigMessage(player, "itemedit.messages.unlockbook-success");
                return true;
            }

            default:
                LowCore.sendConfigMessage(player, "itemedit.messages.invalid-subcommand");
                return true;
        }
    }

    private boolean handleBookSign(Player player, ItemStack item, String[] args) {
        if (item.getType() != Material.WRITABLE_BOOK && item.getType() != Material.WRITTEN_BOOK) {
            LowCore.sendConfigMessage(player, "itemedit.messages.book-not-book");
            return true;
        }

        if (!(item.getItemMeta() instanceof BookMeta)) {
            LowCore.sendConfigMessage(player, "itemedit.messages.book-meta-missing");
            return true;
        }

        if (args.length < 3) {
            LowCore.sendConfigMessage(player, "itemedit.messages.book-sign-usage");
            return true;
        }

        String authorName = join(args, 2);

        BookMeta meta = (BookMeta) item.getItemMeta();
        if (meta == null) {
            LowCore.sendConfigMessage(player, "itemedit.messages.book-meta-missing");
            return true;
        }

        meta.setAuthor(authorName);

        if (!meta.hasTitle()) {
            String rawTitle = plugin.getConfig().getString(
                    "itemedit.book-sign-title",
                    "&fSigned Book"
            );
            String coloredTitle = ChatColor.stripColor(color(rawTitle));
            meta.setTitle(coloredTitle);
        }

        // make sure it becomes a real signed book
        item.setType(Material.WRITTEN_BOOK);
        item.setItemMeta(meta);

        LowCore.sendConfigMessage(player, "itemedit.messages.book-signed",
                "author", authorName
        );
        return true;
    }

    private String join(String[] array, int start) {
        return String.join(" ", Arrays.copyOfRange(array, start, array.length));
    }

    private String color(String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private String prettifyMaterial(Material material) {
        String name = material.name().toLowerCase(Locale.ROOT).replace("_", " ");
        String[] parts = name.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) continue;
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            if (parts[i].length() > 1) {
                builder.append(parts[i].substring(1));
            }
            if (i + 1 < parts.length) builder.append(" ");
        }
        return builder.toString();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                                @NotNull Command command,
                                                @NotNull String alias,
                                                @NotNull String[] args) {

        if (!sender.hasPermission(plugin.getConfig().getString("itemedit.permission", "lowcore.itemedit"))) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> base = Arrays.asList("name", "resetname", "lore", "sign", "book", "unlockbook");
            List<String> result = new ArrayList<>();
            String current = args[0].toLowerCase(Locale.ROOT);
            for (String s : base) {
                if (s.startsWith(current)) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("lore")) {
            List<String> base = Arrays.asList("add", "set", "clear");
            List<String> result = new ArrayList<>();
            String current = args[1].toLowerCase(Locale.ROOT);
            for (String s : base) {
                if (s.startsWith(current)) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("book")) {
            List<String> base = Arrays.asList("sign");
            List<String> result = new ArrayList<>();
            String current = args[1].toLowerCase(Locale.ROOT);
            for (String s : base) {
                if (s.startsWith(current)) {
                    result.add(s);
                }
            }
            return result;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("lore") && args[1].equalsIgnoreCase("set")) {
            return Arrays.asList("1", "2", "3", "4");
        }

        return new ArrayList<>();
    }
}
