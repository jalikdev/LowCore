package dev.jalikdev.lowCore.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerUtils {

    private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static OfflinePlayer getOfflinePlayerRobust(String nameOrUUID) {
        String input = nameOrUUID;

        Player onlinePlayer = Bukkit.getPlayerExact(input);
        if (onlinePlayer != null) {
            return onlinePlayer;
        }

        if (input.startsWith(".")) {
            onlinePlayer = Bukkit.getPlayerExact(input.substring(1));
            if (onlinePlayer != null) {
                return onlinePlayer;
            }
        }


        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(input);

        // Return if player has data
        if (offlinePlayer.hasPlayedBefore()) {
            return offlinePlayer;
        }

        if (input.startsWith(".")) {
            String nameWithoutPrefix = input.substring(1);
            offlinePlayer = Bukkit.getOfflinePlayer(nameWithoutPrefix);

            if (offlinePlayer.hasPlayedBefore()) {
                return offlinePlayer;
            }
        }

        if (UUID_PATTERN.matcher(input).matches()) {
            try {
                UUID uuid = UUID.fromString(input);
                return Bukkit.getOfflinePlayer(uuid);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return Bukkit.getOfflinePlayer(input);
    }
}