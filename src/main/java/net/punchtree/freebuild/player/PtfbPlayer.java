package net.punchtree.freebuild.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
}
