package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TowerDefensePlayer {

    private final Player player;
    private final TowerDefenseGame game;
    private Block selectedTowerBuildLocation;

    public TowerDefensePlayer(Player player, TowerDefenseGame game) {
        this.player = player;
        this.game = game;
    }

    public void setSelectedTowerBuildLocation(Block location) {
        this.selectedTowerBuildLocation = location;
    }

    public void attemptPlaceTower(TowerType basic) {
        game.attemptPlaceTower(this, basic, selectedTowerBuildLocation);
    }

    public Player getPlayer() {
        return player;
    }

}
