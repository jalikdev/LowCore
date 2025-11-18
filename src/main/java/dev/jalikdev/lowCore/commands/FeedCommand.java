package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import dev.jalikdev.lowCore.LowCore;
import org.jetbrains.annotations.NotNull;

public class FeedCommand implements CommandExecutor {

    private final LowCore plugin;

    public FeedCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("&cOnly players can use this command!");
                return true;
            }

            Player player = (Player) sender;

            if (!player.hasPermission("lowcore.feed")) {
                LowCore.sendConfigMessage(sender, "feed.no-permission");
                return true;
            }

            player.setFoodLevel(20);
            player.setSaturation(20);
            LowCore.sendConfigMessage(player, "feed.self");
            return true;
        }

        if(!sender.hasPermission("lowcore.feed.others")) {
            LowCore.sendConfigMessage(sender, "feed.no-permission");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            LowCore.sendConfigMessage(sender, "feed.player-not-found");
            return true;
        }

        target.setFoodLevel(20);
        target.setSaturation(20);

        String msgOther = plugin.formatMessage("feed.other", "player", target.getName());
        String msgTarget = plugin.formatMessage("feed.target", "feeder", sender.getName());

        sender.sendMessage(msgOther);
        target.sendMessage(msgTarget);

        return true;
    }
}