package net.punchtree.freebuild.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public class PtfbPlayer {
    private static final Map<UUID, PtfbPlayer> ptfbPlayers = new HashMap<>();
    private final PlayerDataHandler playerDataHandler;
    private final UUID uuid;


    private PtfbPlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.playerDataHandler = new PlayerDataHandler(uuid);
        ptfbPlayers.put(uuid, this);
    }

    public static PtfbPlayer get(Player player) {
        UUID uuid = player.getUniqueId();
        PtfbPlayer ptfbPlayer = ptfbPlayers.get(uuid);

        if (ptfbPlayer == null) {
            ptfbPlayer = new PtfbPlayer(player);
        }

        return ptfbPlayer;
    }

    public static void remove(PtfbPlayer ptfbPlayer) {
        ptfbPlayers.remove(ptfbPlayer.getUuid());
    }

    public UUID getUuid() {
        return uuid;
    }

    public @Nullable Player getBukkitPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public PlayerDataHandler getPlayerDataHandler() {
        return playerDataHandler;
    }

    public void withDataLoaded(Consumer<PlayerDataHandler> action) {
        playerDataHandler.withDataLoaded(action);
    }

    public Optional<Object> getPlayerData(String table, String key) {
        return playerDataHandler.getPlayerData(table, key);
    }

    public void setPlayerData(String table, String key, Object value) {
        playerDataHandler.setPlayerData(table, key, value);
    }

    public void saveData() {
        playerDataHandler.saveData();
    }

}
