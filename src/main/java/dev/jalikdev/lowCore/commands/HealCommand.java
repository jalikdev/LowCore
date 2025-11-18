package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

public class HealCommand implements CommandExecutor {

    private final LowCore plugin;

    public HealCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("&cOnly players can use this command.");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lowcore.heal")) {
                LowCore.sendConfigMessage(player, "heal.noPermission");
                return true;
            }

            player.setHealth(player.getMaxHealth());
            player.setFireTicks(0);
            LowCore.sendConfigMessage(player, "heal.self");
            return true;
        }

        if (!sender.hasPermission("lowcore.heal.others")) {
            LowCore.sendConfigMessage(sender, "heal.no-permission");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            LowCore.sendConfigMessage(sender, "heal.player-not-found");
            return true;
        }

        target.setHealth(target.getMaxHealth());
        target.setFireTicks(0);

        String msgOther = plugin.formatMessage("heal.other", "player", target.getName());
        String msgTarget = plugin.formatMessage("heal.target", "healer", sender.getName());

        sender.sendMessage(msgOther);
        target.sendMessage(msgTarget);

        return true;
    }
}