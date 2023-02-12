package net.punchtree.freebuild.towerdefense;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TowerDefensePlayerManager {

    // TODO make sure to remove players when they leave the server

    private final Map<UUID, TowerDefensePlayer> playersMap = new HashMap<>();

    @Deprecated
    public void registerPlayer(Player player) {
        playersMap.putIfAbsent(player.getUniqueId(), new TowerDefensePlayer(player, null));
    }

    public TowerDefensePlayer getPlayer(Player player) {
        return playersMap.get(player.getUniqueId());
    }

}
