package dev.jalikdev.lowCore.commands;

import dev.jalikdev.lowCore.LowCore;
import dev.jalikdev.lowCore.world.WorldCreationSession;
import dev.jalikdev.lowCore.world.WorldCreationSession.State;
import dev.jalikdev.lowCore.world.WorldInventoryManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;

public class WorldCommand implements CommandExecutor, TabExecutor, Listener {

    private static final int SLOT_ENVIRONMENT = 10;
    private static final int SLOT_WORLD_TYPE = 12;
    private static final int SLOT_GAMEMODE = 14;
    private static final int SLOT_SEED = 16;
    private static final int SLOT_INVENTORY = 20;
    private static final int SLOT_CREATE = 22;

    private final LowCore plugin;
    private final WorldInventoryManager inventoryManager;
    private final Map<UUID, WorldCreationSession> sessions = new HashMap<>();

    public WorldCommand(LowCore plugin) {
        this.plugin = plugin;
        this.inventoryManager = plugin.getWorldInventoryManager();
    }

    private String getMainGuiTitle() {
        return plugin.getMessageRaw("world.gui-main-title");
    }

    private String getSetupGuiTitleBase() {
        return plugin.getMessageRaw("world.gui-setup-title");
    }

    private String getGroupKeyForWorld(String worldName) {
        String path = "world.worlds." + worldName + ".shared-inventory";
        boolean shared = plugin.getConfig().getBoolean(path, true);
        return shared ? "shared" : worldName;
    }

    private String getGroupKeyForNewWorld(String worldName, boolean sharedInventory) {
        return sharedInventory ? "shared" : worldName;
    }

    private boolean isProtectedWorld(String worldName) {
        List<String> protectedList = plugin.getConfig().getStringList("world.protected-worlds");
        if (protectedList == null) {
            protectedList = new ArrayList<>();
        }
        if (!protectedList.contains("world")) {
            protectedList.add("world");
        }
        if (!protectedList.contains("world_nether")) {
            protectedList.add("world_nether");
        }
        if (!protectedList.contains("world_the_end")) {
            protectedList.add("world_the_end");
        }
        return protectedList.contains(worldName);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return true;
        }

        if (!player.hasPermission("lowcore.world")) {
            LowCore.sendConfigMessage(player, "world.no-permission");
            return true;
        }

        openMainGui(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList();
    }

    private void openMainGui(Player player) {
        Inventory inv = Bukkit.createInventory(player, 54, getMainGuiTitle());
        fillWithPane(inv);

        List<World> worlds = Bukkit.getWorlds();
        int slot = 10;

        for (World world : worlds) {
            if (slot >= 44) {
                break;
            }
            ItemStack item = createWorldItem(world);
            inv.setItem(slot, item);
            slot++;
            if (slot % 9 == 0) {
                slot += 2;
            }
        }

        if (player.hasPermission("lowcore.world.create")) {
            ItemStack createItem = new ItemStack(Material.NETHER_STAR);
            ItemMeta meta = createItem.getItemMeta();
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("world.gui-create-button-name", "&aCreate new world")));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("world.gui-create-button-lore", "&7Create a new world")));
            if (player.hasPermission("lowcore.world.delete")) {
                lore.add(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("world.gui-delete-hint", "&7Shift-Click on a world to delete it")));
            }
            meta.setLore(lore);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            createItem.setItemMeta(meta);
            inv.setItem(49, createItem);
        }

        player.openInventory(inv);
    }

    private ItemStack createWorldItem(World world) {
        Material material;
        if (world.getEnvironment() == World.Environment.NETHER) {
            material = Material.NETHERRACK;
        } else if (world.getEnvironment() == World.Environment.THE_END) {
            material = Material.END_STONE;
        } else {
            material = Material.GRASS_BLOCK;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + world.getName());

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Environment: " + world.getEnvironment().name());
        lore.add(ChatColor.GRAY + "WorldType: " + world.getWorldType().name());
        lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-world-item-lore-click", "&eClick to teleport to the world spawn")));
        if (plugin.getConfig().getBoolean("world.show-gamemode-in-lore", true)) {
            String gmPath = "world.worlds." + world.getName() + ".gamemode";
            String gmName = plugin.getConfig().getString(gmPath, "DEFAULT");
            lore.add(ChatColor.GRAY + "Gamemode: " + gmName);
        }
        if (plugin.getConfig().getBoolean("world.show-shared-inventory-in-lore", true)) {
            String invPath = "world.worlds." + world.getName() + ".shared-inventory";
            boolean shared = plugin.getConfig().getBoolean(invPath, true);
            lore.add(ChatColor.GRAY + "Shared inventory: " + shared);
        }

        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private void fillWithPane(Inventory inv) {
        ItemStack pane = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = pane.getItemMeta();
        meta.setDisplayName(" ");
        pane.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }
    }

    private void openSetupGui(Player player) {
        WorldCreationSession session = sessions.get(player.getUniqueId());
        if (session == null || session.getName() == null) {
            LowCore.sendConfigMessage(player, "world.session-error");
            return;
        }

        String title = getSetupGuiTitleBase() + ": " + session.getName();
        Inventory inv = Bukkit.createInventory(player, 27, title);
        fillWithPane(inv);

        ItemStack envItem = createEnvironmentItem(session);
        ItemStack typeItem = createWorldTypeItem(session);
        ItemStack gmItem = createGamemodeItem(session);
        ItemStack seedItem = createSeedItem(session);
        ItemStack inventoryItem = createInventorySharedItem(session);
        ItemStack createItem = createCreateButtonItem();

        inv.setItem(SLOT_ENVIRONMENT, envItem);
        inv.setItem(SLOT_WORLD_TYPE, typeItem);
        inv.setItem(SLOT_GAMEMODE, gmItem);
        inv.setItem(SLOT_SEED, seedItem);
        inv.setItem(SLOT_INVENTORY, inventoryItem);
        inv.setItem(SLOT_CREATE, createItem);

        player.openInventory(inv);
    }

    private ItemStack createEnvironmentItem(WorldCreationSession session) {
        Material material;
        String name;
        if (session.getEnvironment() == World.Environment.NETHER) {
            material = Material.NETHERRACK;
            name = "Nether";
        } else if (session.getEnvironment() == World.Environment.THE_END) {
            material = Material.END_STONE;
            name = "End";
        } else {
            material = Material.GRASS_BLOCK;
            name = "Normal";
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Environment: " + name);
        meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-environment-item-lore", "&7Click to cycle environment"))));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createWorldTypeItem(WorldCreationSession session) {
        Material material = Material.MAP;
        String name = session.getWorldType().name();

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "World type: " + name);
        meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-worldtype-item-lore", "&7Click to cycle world type"))));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createGamemodeItem(WorldCreationSession session) {
        Material material = Material.IRON_SWORD;
        String name = session.getGameMode().name();

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Gamemode: " + name);
        meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-gamemode-item-lore", "&7Click to cycle gamemode"))));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createSeedItem(WorldCreationSession session) {
        Material material = Material.WHEAT_SEEDS;

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        String value = session.getSeed() == null ? "random" : session.getSeed().toString();
        meta.setDisplayName(ChatColor.AQUA + "Seed: " + value);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-seed-item-lore1", "&7Click to set seed via chat")));
        lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-seed-item-lore2", "&7Type 'random' for a random seed")));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInventorySharedItem(WorldCreationSession session) {
        boolean shared = session.isSharedInventory();
        Material material = shared ? Material.CHEST : Material.ENDER_CHEST;
        String state = shared ? "Shared" : "Separated";

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.AQUA + "Inventory: " + state);
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-inventory-item-lore1", "&7Click to toggle inventory mode")));
        lore.add(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-inventory-item-lore2", "&7Shared: same inventory across worlds")));
        meta.setLore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createCreateButtonItem() {
        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-create-final-name", "&aCreate world")));
        meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-create-final-lore", "&eClick to generate the world"))));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory inv = event.getInventory();
        String title = event.getView().getTitle();

        String mainTitle = getMainGuiTitle();
        String setupBase = getSetupGuiTitleBase();

        if (!title.startsWith(mainTitle) && !title.startsWith(setupBase)) {
            return;
        }

        event.setCancelled(true);

        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) {
            return;
        }

        if (title.startsWith(mainTitle)) {
            handleMainGuiClick(player, current, event.getClick());
        } else if (title.startsWith(setupBase)) {
            handleSetupGuiClick(player, event.getRawSlot());
        }
    }

    private void handleMainGuiClick(Player player, ItemStack current, ClickType clickType) {
        if (!current.hasItemMeta() || !current.getItemMeta().hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(current.getItemMeta().getDisplayName());
        String createName = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("world.gui-create-button-name", "&aCreate new world")));

        if (displayName.equalsIgnoreCase(ChatColor.stripColor(createName))) {
            if (!player.hasPermission("lowcore.world.create")) {
                LowCore.sendConfigMessage(player, "world.no-create-permission");
                return;
            }
            WorldCreationSession session = new WorldCreationSession(player.getUniqueId());
            sessions.put(player.getUniqueId(), session);
            player.closeInventory();
            LowCore.sendConfigMessage(player, "world.chat-name-prompt");
            return;
        }

        World world = Bukkit.getWorld(displayName);
        if (world == null) {
            LowCore.sendConfigMessage(player, "world.world-missing");
            return;
        }

        if (clickType.isShiftClick() && player.hasPermission("lowcore.world.delete")) {
            deleteWorld(player, world);
            return;
        }

        String fromWorld = player.getWorld().getName();
        String fromGroup = getGroupKeyForWorld(fromWorld);
        inventoryManager.savePlayerInventory(player, fromGroup);

        Location spawn = world.getSpawnLocation();
        if (spawn == null) {
            spawn = new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5);
        }
        player.teleport(spawn);

        String toGroup = getGroupKeyForWorld(world.getName());
        inventoryManager.loadPlayerInventory(player, toGroup);

        applyWorldGamemode(player, world.getName());
        LowCore.sendConfigMessage(player, "world.teleport-success", "world", world.getName());
    }

    private void handleSetupGuiClick(Player player, int slot) {
        WorldCreationSession session = sessions.get(player.getUniqueId());
        if (session == null) {
            player.closeInventory();
            return;
        }

        if (slot == SLOT_ENVIRONMENT) {
            cycleEnvironment(session);
            openSetupGui(player);
        } else if (slot == SLOT_WORLD_TYPE) {
            cycleWorldType(session);
            openSetupGui(player);
        } else if (slot == SLOT_GAMEMODE) {
            cycleGamemode(session);
            openSetupGui(player);
        } else if (slot == SLOT_SEED) {
            session.setState(State.AWAITING_SEED);
            player.closeInventory();
            LowCore.sendConfigMessage(player, "world.chat-seed-prompt");
        } else if (slot == SLOT_INVENTORY) {
            session.setSharedInventory(!session.isSharedInventory());
            openSetupGui(player);
        } else if (slot == SLOT_CREATE) {
            createWorldFromSession(player, session);
        }
    }

    private void cycleEnvironment(WorldCreationSession session) {
        World.Environment env = session.getEnvironment();
        if (env == World.Environment.NORMAL) {
            session.setEnvironment(World.Environment.NETHER);
        } else if (env == World.Environment.NETHER) {
            session.setEnvironment(World.Environment.THE_END);
        } else {
            session.setEnvironment(World.Environment.NORMAL);
        }
    }

    private void cycleWorldType(WorldCreationSession session) {
        WorldType type = session.getWorldType();
        if (type == WorldType.NORMAL) {
            session.setWorldType(WorldType.FLAT);
        } else if (type == WorldType.FLAT) {
            session.setWorldType(WorldType.LARGE_BIOMES);
        } else if (type == WorldType.LARGE_BIOMES) {
            session.setWorldType(WorldType.AMPLIFIED);
        } else {
            session.setWorldType(WorldType.NORMAL);
        }
    }

    private void cycleGamemode(WorldCreationSession session) {
        GameMode gm = session.getGameMode();
        if (gm == GameMode.SURVIVAL) {
            session.setGameMode(GameMode.CREATIVE);
        } else if (gm == GameMode.CREATIVE) {
            session.setGameMode(GameMode.ADVENTURE);
        } else if (gm == GameMode.ADVENTURE) {
            session.setGameMode(GameMode.SPECTATOR);
        } else {
            session.setGameMode(GameMode.SURVIVAL);
        }
    }

    private void createWorldFromSession(Player player, WorldCreationSession session) {
        if (session.getName() == null || session.getName().isEmpty()) {
            LowCore.sendConfigMessage(player, "world.no-name");
            return;
        }

        String name = session.getName();
        if (Bukkit.getWorld(name) != null) {
            LowCore.sendConfigMessage(player, "world.name-already-exists");
            return;
        }

        WorldCreator creator = new WorldCreator(name);
        creator.environment(session.getEnvironment());
        creator.type(session.getWorldType());
        if (session.getSeed() != null) {
            creator.seed(session.getSeed());
        }

        String fromWorld = player.getWorld().getName();
        String fromGroup = getGroupKeyForWorld(fromWorld);
        inventoryManager.savePlayerInventory(player, fromGroup);

        World world = creator.createWorld();
        if (world == null) {
            LowCore.sendConfigMessage(player, "world.create-failed");
            return;
        }

        String basePath = "world.worlds." + name;
        plugin.getConfig().set(basePath + ".gamemode", session.getGameMode().name());
        plugin.getConfig().set(basePath + ".shared-inventory", session.isSharedInventory());
        plugin.saveConfig();

        String toGroup = getGroupKeyForNewWorld(name, session.isSharedInventory());
        inventoryManager.loadPlayerInventory(player, toGroup);

        Location spawn = world.getSpawnLocation();
        if (spawn == null) {
            spawn = new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5);
        }

        player.teleport(spawn);
        player.setGameMode(session.getGameMode());
        LowCore.sendConfigMessage(player, "world.create-success", "world", name);
        sessions.remove(player.getUniqueId());
    }

    private void applyWorldGamemode(Player player, String worldName) {
        String path = "world.worlds." + worldName + ".gamemode";
        String gmString = plugin.getConfig().getString(path, null);
        if (gmString == null || gmString.equalsIgnoreCase("DEFAULT")) {
            return;
        }
        try {
            GameMode gm = GameMode.valueOf(gmString.toUpperCase(Locale.ROOT));
            player.setGameMode(gm);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void deleteWorld(Player player, World world) {
        String mainWorldName = Bukkit.getWorlds().get(0).getName();
        if (world.getName().equals(mainWorldName) || isProtectedWorld(world.getName())) {
            LowCore.sendConfigMessage(player, "world.delete-main-deny");
            return;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getWorld().equals(world)) {
                World fallback = Bukkit.getWorld(mainWorldName);
                String fromGroup = getGroupKeyForWorld(world.getName());
                inventoryManager.savePlayerInventory(p, fromGroup);

                Location spawn = fallback.getSpawnLocation();
                if (spawn == null) {
                    spawn = new Location(fallback, 0.5, fallback.getHighestBlockYAt(0, 0) + 1, 0.5);
                }
                p.teleport(spawn);

                String toGroup = getGroupKeyForWorld(fallback.getName());
                inventoryManager.loadPlayerInventory(p, toGroup);
                applyWorldGamemode(p, fallback.getName());
            }
        }

        boolean unloaded = Bukkit.unloadWorld(world, true);
        if (!unloaded) {
            LowCore.sendConfigMessage(player, "world.delete-unload-failed");
            return;
        }

        File folder = world.getWorldFolder();
        deleteFolder(folder);

        String basePath = "world.worlds." + world.getName();
        if (plugin.getConfig().contains(basePath)) {
            plugin.getConfig().set(basePath, null);
            plugin.saveConfig();
        }

        LowCore.sendConfigMessage(player, "world.delete-success", "world", world.getName());
        openMainGui(player);
    }

    private void deleteFolder(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteFolder(child);
                }
            }
        }
        file.delete();
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        WorldCreationSession session = sessions.get(uuid);
        if (session == null) {
            return;
        }

        if (session.getState() == State.AWAITING_NAME) {
            event.setCancelled(true);
            String msg = event.getMessage().trim();
            if (msg.isEmpty()) {
                LowCore.sendConfigMessage(player, "world.name-empty");
                return;
            }
            if (Bukkit.getWorld(msg) != null) {
                LowCore.sendConfigMessage(player, "world.name-already-exists");
                return;
            }
            session.setName(msg);
            session.setState(State.IN_GUI);
            Bukkit.getScheduler().runTask(plugin, () -> openSetupGui(player));
            LowCore.sendConfigMessage(player, "world.name-set", "world", msg);
        } else if (session.getState() == State.AWAITING_SEED) {
            event.setCancelled(true);
            String msg = event.getMessage().trim();
            if (msg.equalsIgnoreCase("random") || msg.isEmpty()) {
                session.setSeed(null);
                session.setState(State.IN_GUI);
                Bukkit.getScheduler().runTask(plugin, () -> openSetupGui(player));
                LowCore.sendConfigMessage(player, "world.seed-random");
                return;
            }
            try {
                long seed = Long.parseLong(msg);
                session.setSeed(seed);
                session.setState(State.IN_GUI);
                Bukkit.getScheduler().runTask(plugin, () -> openSetupGui(player));
                LowCore.sendConfigMessage(player, "world.seed-set", "seed", String.valueOf(seed));
            } catch (NumberFormatException ex) {
                LowCore.sendConfigMessage(player, "world.seed-invalid");
            }
        }
    }
}
