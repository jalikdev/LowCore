package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SudoCommand implements CommandExecutor, TabCompleter {

    private final LowCore plugin;

    public SudoCommand(LowCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        boolean hasNormalSudo = sender.hasPermission("lowcore.sudo");
        boolean hasOpSudo = sender.hasPermission("lowcore.sudo.op");

        if (!hasNormalSudo && !hasOpSudo) {
            LowCore.sendConfigMessage(sender, "no-permission");
            return true;
        }

        if (args.length < 2) {
            LowCore.sendConfigMessage(sender, "sudo.usage");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            LowCore.sendConfigMessage(sender, "unknown-player");
            return true;
        }

        if (!hasOpSudo) {
            if (target.isOp() || target.hasPermission("lowcore.sudo.op")) {
                LowCore.sendConfigMessage(sender, "sudo.blocked-target");
                return true;
            }
        }

        String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        boolean isCommand = input.startsWith("/");

        if (isCommand) {
            String cmdLine = input.substring(1);

            if (cmdLine.trim().isEmpty()) {
                LowCore.sendConfigMessage(sender, "sudo.no-command");
                return true;
            }

            String[] split = cmdLine.split(" ", 2);
            String baseLabel = split[0];
            String lowerBase = baseLabel.toLowerCase(Locale.ROOT);

            if (lowerBase.equals("sudo") || lowerBase.endsWith(":sudo")) {
                LowCore.sendConfigMessage(sender, "sudo.blocked-sudo-command");
                return true;
            }

            if (lowerBase.equals("stop") ||
                    lowerBase.equals("shutdown") ||
                    lowerBase.equals("op") ||
                    lowerBase.endsWith(":stop") ||
                    lowerBase.endsWith(":shutdown") ||
                    lowerBase.endsWith(":op")) {

                LowCore.sendConfigMessage(sender, "sudo.blocked-stop");
                return true;
            }

            if (!hasOpSudo) {
                PluginCommand pluginCommand = Bukkit.getPluginCommand(baseLabel);

                if (pluginCommand != null) {
                    if (!pluginCommand.testPermissionSilent(sender)) {
                        LowCore.sendConfigMessage(sender, "sudo.blocked-command");
                        return true;
                    }
                } else {
                    if (!sender.isOp()) {
                        LowCore.sendConfigMessage(sender, "sudo.blocked-command");
                        return true;
                    }
                }
            }

            target.performCommand(cmdLine);

        } else {
            target.chat(input);
        }

        LowCore.sendConfigMessage(sender, "sudo.forced",
                "target", target.getName(),
                "input", input);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String current = args[0].toLowerCase(Locale.ROOT);

            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName();
                if (current.isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(current)) {
                    suggestions.add(name);
                }
            }
        }

        return suggestions;
    }
}
