package dev.jalik.lowcore.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SudoCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length < 2) {
            sender.sendMessage("§cBenutzung: /sudo <Spieler> <Command/Nachricht>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);

        if (target == null) {
            sender.sendMessage("§cSpieler nicht gefunden.");
            return true;
        }

        String input = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        if (input.startsWith("/")) {

            String cmd = input.substring(1);
            target.performCommand(cmd);
            sender.sendMessage("§aSudo ausgeführt: §e" + target.getName() + " §7→ §f/" + cmd);
        } else {
            target.chat(input);
            sender.sendMessage("§aSudo Chat: §e" + target.getName() + " §7→ §f" + input);
        }

        return true;
    }
}
