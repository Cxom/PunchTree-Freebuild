package net.punchtree.freebuild.towerdefense;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TowerDefensePlayerManager {

    private final Map<UUID, TowerDefensePlayer> playersMap = new HashMap<>();


    public void registerPlayer(Player player, TowerDefenseGame game) {
        playersMap.putIfAbsent(player.getUniqueId(), new TowerDefensePlayer(player, game));
    }

    public TowerDefensePlayer getPlayer(Player player) {
        return playersMap.get(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        // TODO pass the removal to the game!!!
        playersMap.remove(player.getUniqueId());
    }
}
