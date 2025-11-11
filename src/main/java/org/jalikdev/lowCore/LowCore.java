package org.jalikdev.lowCore;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jalikdev.lowCore.commands.FlyCommand;
import org.jalikdev.lowCore.commands.GmCommand;
import org.jalikdev.lowCore.commands.LowcoreCommand;
import org.jalikdev.lowCore.commands.EcCommand;
import org.jalikdev.lowCore.commands.InvseeCommand;
import org.jalikdev.lowCore.commands.HatCommand;
import org.jalikdev.lowCore.commands.FeedCommand;
import org.jalikdev.lowCore.commands.HealCommand;
import org.jalikdev.lowCore.commands.SpawnMobCommand;
import org.jalikdev.lowCore.commands.EnchantCommand;
import org.jalikdev.lowCore.commands.AnvilCommand;
import org.jalikdev.lowCore.commands.RepairCommand;

import java.util.Objects;

public class LowCore extends JavaPlugin {

    public static final String DEFAULT_PREFIX = "&8[&aLowCore&8] &7";

    private static LowCore instance;
    private String prefix;

    public static LowCore getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        loadPrefix();

        getLogger().info("LowCore plugin enabled!");
        getLogger().info("Configuration loaded.");

        LowcoreCommand lowcoreCommand = new LowcoreCommand(this);
        Objects.requireNonNull(getCommand("lowcore")).setExecutor(lowcoreCommand);
        Objects.requireNonNull(getCommand("lowcore")).setTabCompleter(lowcoreCommand);

        InvseeCommand invseeCommand = new InvseeCommand(this);
        Objects.requireNonNull(getCommand("invsee")).setExecutor(invseeCommand);
        Objects.requireNonNull(getCommand("invsee")).setTabCompleter(invseeCommand);
        getServer().getPluginManager().registerEvents(invseeCommand, this);

        GmCommand gmCommand = new GmCommand();
        Objects.requireNonNull(getCommand("gm")).setExecutor(gmCommand);
        Objects.requireNonNull(getCommand("gm")).setTabCompleter(gmCommand);

        EcCommand ecCommand = new EcCommand();
        Objects.requireNonNull(getCommand("ec")).setExecutor(ecCommand);
        Objects.requireNonNull(getCommand("ec")).setTabCompleter(ecCommand);

        HatCommand hatCommand = new HatCommand();
        Objects.requireNonNull(getCommand("hat")).setExecutor(hatCommand);
        Objects.requireNonNull(getCommand("hat")).setTabCompleter(hatCommand);

        FlyCommand flyCommand = new FlyCommand();
        Objects.requireNonNull(getCommand("fly")).setExecutor(flyCommand);

        HealCommand healCommand = new HealCommand(this);
        Objects.requireNonNull(getCommand("heal")).setExecutor(healCommand);

        FeedCommand feedCommand = new FeedCommand(this);
        Objects.requireNonNull(getCommand("feed")).setExecutor(feedCommand);

        SpawnMobCommand spawnMobCommand = new SpawnMobCommand(this);
        Objects.requireNonNull(getCommand("spawnmob")).setExecutor(spawnMobCommand);
        Objects.requireNonNull(getCommand("spawnmob")).setTabCompleter(spawnMobCommand);

        EnchantCommand enchantCommand = new EnchantCommand(this);
        Objects.requireNonNull(getCommand("enchant")).setExecutor(enchantCommand);
        Objects.requireNonNull(getCommand("enchant")).setTabCompleter(enchantCommand);

        AnvilCommand anvilCommand = new AnvilCommand();
        Objects.requireNonNull(getCommand("anvil")).setExecutor(anvilCommand);

        RepairCommand repairCommand = new RepairCommand();
        Objects.requireNonNull(getCommand("repair")).setExecutor(repairCommand);

    }

    @Override
    public void onDisable() {
        getLogger().info("LowCore plugin disabled.");
    }

    private void loadPrefix() {
        String raw = getConfig().getString("prefix", DEFAULT_PREFIX);
        this.prefix = ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getPrefix() {
        return prefix != null ? prefix : ChatColor.translateAlternateColorCodes('&', DEFAULT_PREFIX);
    }


    public String getMessageRaw(String key) {
        String raw;

        if (getConfig().contains("messages." + key)) {
            raw = getConfig().getString("messages." + key);
        }

        else if (key.contains(".")) {
            String[] parts = key.split("\\.");
            if (parts.length == 2 && getConfig().contains(parts[0] + ".messages." + parts[1])) {
                raw = getConfig().getString(parts[0] + ".messages." + parts[1]);
            } else {
                raw = "&cMissing message: " + key;
            }
        }

        else {
            raw = "&cMissing message: " + key;
        }

        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getMessage(String key) {
        return getPrefix() + getMessageRaw(key);
    }

    public String formatMessage(String key, String... placeholders) {
        String msg = getMessage(key);

        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            String placeholder = "%" + placeholders[i] + "%";
            msg = msg.replace(placeholder, placeholders[i + 1]);
        }

        return msg;
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null) return;

        if (instance == null) {
            String full = DEFAULT_PREFIX + message;
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', full));
            return;
        }

        String full = instance.getPrefix() + message;
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', full));
    }

    public static void sendConfigMessage(CommandSender sender, String key, String... placeholders) {
        if (sender == null || instance == null) return;

        String msg = instance.formatMessage(key, placeholders);
        sender.sendMessage(msg);
    }

    public void reloadLowCoreConfig(CommandSender sender) {
        reloadConfig();
        loadPrefix();
        sendConfigMessage(sender, "reload");
        getLogger().info("Configuration reloaded by " + sender.getName());
    }
}
