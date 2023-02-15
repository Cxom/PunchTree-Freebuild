package net.punchtree.freebuild.towerdefense;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class TowerDefenseQuitListener implements Listener {

    private TowerDefensePlayerManager towerDefensePlayerManager;

    public TowerDefenseQuitListener(TowerDefensePlayerManager towerDefensePlayerManager) {
        this.towerDefensePlayerManager = towerDefensePlayerManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        towerDefensePlayerManager.removePlayer(event.getPlayer());
    }

}
