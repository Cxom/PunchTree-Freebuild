package net.punchtree.freebuild.towerdefense.tower;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.towerdefense.Range;
import net.punchtree.freebuild.towerdefense.TowerDefenseGame;
import net.punchtree.freebuild.towerdefense.TowerType;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class DirectDamageTower implements Tower {

    private TowerDefenseGame game;
    private TowerType type;
    private final Range range;
    private final int ticksBetweenShots = 5;

    public DirectDamageTower(TowerDefenseGame game, Block selectedTowerBuildBlock, TowerType type) {
        this.game = game;
        this.type = type;

        Location center = selectedTowerBuildBlock.getLocation().add(0.5, 0, 0.5);
        this.range = new Range(center, 4.5);
    }

    int tickCounter = 0;
    @Override
    public void tick() {
        // direct damage to enemies
        // get the furthest along enemy THAT IS WITHIN the range of this tower
        // then do damage to it
        ++tickCounter;
        if ((tickCounter %= ticksBetweenShots) != 0) return;
        game.getFurthestMinionWithinRange(range).ifPresent(minion -> {
            minion.damage(1);
            ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.FALLING_DUST).data(type.getIcon().getType().createBlockData()));
            ParticleShapes.spawnParticleLine(minion.getLocation(), range.center(), (int) (minion.getLocation().distance(range.center()) * 2));
            range.center().getWorld().playSound(range.center(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1, 1);
        });

        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(Color.WHITE));
        range.draw();
    }


}
