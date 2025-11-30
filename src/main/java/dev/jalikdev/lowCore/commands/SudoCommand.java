package dev.jalikdev.lowCore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SudoCommand implements CommandExecutor, TabCompleter {

    private final String PERM_BASIC = "lowcore.sudo";
    private final String PERM_OP_BYPASS = "lowcore.sudo.op";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(PERM_BASIC)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /sudo <player> <command or message>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cThat player is not online.");
            return true;
        }

        if (target.getName().equals(sender.getName())) {
            sender.sendMessage("§cYou cannot sudo yourself.");
            return true;
        }

        if (!sender.hasPermission(PERM_OP_BYPASS)) {
            if (target.isOp()) {
                sender.sendMessage("§cYou cannot sudo an operator or a player of equal/higher rank.");
                return true;
            }
            if (target.hasPermission(PERM_OP_BYPASS)) {
                sender.sendMessage("§cYou cannot sudo a player with a higher rank.");
                return true;
            }
        }

        String input = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        if (input.startsWith("/")) {
            String cmd = input.substring(1);
            target.performCommand(cmd);

        } else {
            target.chat(input);
        }

        sender.sendMessage("§aForced §e" + target.getName() + " §ato: §f" + input);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            String current = args[0].toLowerCase();

            for (Player p : Bukkit.getOnlinePlayers()) {
                String name = p.getName();
                if (current.isEmpty() || name.toLowerCase().startsWith(current)) {
                    suggestions.add(name);
                }
            }
        }

        return suggestions;
    }
}