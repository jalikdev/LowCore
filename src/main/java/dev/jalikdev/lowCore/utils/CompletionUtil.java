package dev.jalikdev.lowCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CompletionUtil {

    public static List<String> onlinePlayers(String input) {
        String current = input.toLowerCase();
        List<String> result = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase().startsWith(current)) {
                result.add(p.getName());
            }
        }
        return result;
    }
}
