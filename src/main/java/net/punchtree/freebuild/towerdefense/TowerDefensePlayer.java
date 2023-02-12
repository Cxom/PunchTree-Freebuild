package net.punchtree.freebuild.towerdefense;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

public class TowerDefensePlayer {

    private final Player player;
    private final TowerDefenseGame game;

    public TowerDefensePlayer(Player player, TowerDefenseGame game) {
        this.player = player;
        this.game = game;
    }

    public void placeTower(TowerType basic) {
        player.sendMessage("Placing a tower of type " + basic.getName() + "!");
    }

    public Player getPlayer() {
        return player;
    }
}
