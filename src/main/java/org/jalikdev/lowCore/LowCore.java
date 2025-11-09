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
import java.util.Objects;


public class LowCore extends JavaPlugin {

    // Prefix with & color codes
    public static final String PREFIX = "&8[&aLowCore&8] &7";

    @Override
    public void onEnable() {
        getLogger().info("LowCore plugin enabled!");

        LowcoreCommand lowcoreCommand = new LowcoreCommand();
        GmCommand gmCommand = new GmCommand();
        EcCommand ecCommand = new EcCommand();
        InvseeCommand invseeCommand = new InvseeCommand(this);
        HatCommand hatCommand = new HatCommand();

        Objects.requireNonNull(getCommand("lowcore")).setExecutor(lowcoreCommand);
        Objects.requireNonNull(getCommand("lowcore")).setTabCompleter(lowcoreCommand);
        Objects.requireNonNull(getCommand("fly")).setExecutor(new FlyCommand());
        Objects.requireNonNull(getCommand("gm")).setExecutor(gmCommand);
        Objects.requireNonNull(getCommand("gm")).setTabCompleter(gmCommand);
        Objects.requireNonNull(getCommand("ec")).setExecutor(ecCommand);
        Objects.requireNonNull(getCommand("ec")).setTabCompleter(ecCommand);
        Objects.requireNonNull(getCommand("invsee")).setExecutor(invseeCommand);
        Objects.requireNonNull(getCommand("invsee")).setTabCompleter(invseeCommand);
        getServer().getPluginManager().registerEvents(invseeCommand, this);
        Objects.requireNonNull(getCommand("hat")).setExecutor(hatCommand);
        Objects.requireNonNull(getCommand("hat")).setTabCompleter(hatCommand);

    }

    @Override
    public void onDisable() {
        getLogger().info("LowCore plugin disabled.");
    }

    public static void sendMessage(CommandSender sender, String message) {
        if (sender == null || message == null) return;

        String full = PREFIX + message;
        String colored = ChatColor.translateAlternateColorCodes('&', full);
        sender.sendMessage(colored);
    }
}
