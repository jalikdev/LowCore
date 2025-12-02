package dev.jalikdev.lowCore.world;

import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldType;

import java.util.UUID;

public class WorldCreationSession {

    public enum State {
        AWAITING_NAME,
        AWAITING_SEED,
        IN_GUI
    }

    private final UUID playerId;
    private String name;
    private World.Environment environment;
    private WorldType worldType;
    private GameMode gameMode;
    private Long seed;
    private State state;
    private boolean sharedInventory;

    public WorldCreationSession(UUID playerId) {
        this.playerId = playerId;
        this.environment = World.Environment.NORMAL;
        this.worldType = WorldType.NORMAL;
        this.gameMode = GameMode.SURVIVAL;
        this.seed = null;
        this.state = State.AWAITING_NAME;
        this.sharedInventory = true;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public World.Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(World.Environment environment) {
        this.environment = environment;
    }

    public WorldType getWorldType() {
        return worldType;
    }

    public void setWorldType(WorldType worldType) {
        this.worldType = worldType;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public Long getSeed() {
        return seed;
    }

    public void setSeed(Long seed) {
        this.seed = seed;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public boolean isSharedInventory() {
        return sharedInventory;
    }

    public void setSharedInventory(boolean sharedInventory) {
        this.sharedInventory = sharedInventory;
    }
}
