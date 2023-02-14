package net.punchtree.freebuild.towerdefense.tower;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.towerdefense.Minion;
import net.punchtree.freebuild.towerdefense.Range;
import net.punchtree.freebuild.towerdefense.TowerDefenseGame;
import net.punchtree.freebuild.towerdefense.TowerType;
import net.punchtree.freebuild.util.particle.ParticleShapes;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class AoeDamageTower implements Tower {

    private TowerDefenseGame game;
    private TowerType type;
    private final Range range;
    private final int ticksBetweenShots = 7;
    private final double damageScalePerBlockDistance = .5;
    private final double maxDamage = .75;

    public AoeDamageTower(TowerDefenseGame game, Block selectedTowerBuildBlock, TowerType type) {
        this.game = game;
        this.type = type;

        Location center = selectedTowerBuildBlock.getLocation().add(0.5, 0, 0.5);
        this.range = new Range(center, 4.5);
    }

    int tickCounter = 0;
    public void tick() {
        // direct damage to enemies
        // get the furthest along enemy THAT IS WITHIN the range of this tower
        // then do damage to it
        ++tickCounter;
        if ((tickCounter %= ticksBetweenShots) != 0) return;
        game.getFurthestMinionWithinRange(range).ifPresent(furthestMinion -> {
            Location explosionLocation = furthestMinion.getLocation();
            playExplosion(explosionLocation);
            for (Minion minion : game.getMinionsWithinRange(new Range(explosionLocation, maxDamage / damageScalePerBlockDistance))) {
                minion.damage(maxDamage - (explosionLocation.distance(minion.getLocation()) * damageScalePerBlockDistance));
            }
        });

        net.punchtree.util.particle.ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(Color.WHITE));
        range.draw();
    }

    private void playExplosion(Location location) {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.EXPLOSION_NORMAL));
        ParticleShapes.drawCircle(location, 1, (int) (3 * Math.PI));
        range.center().getWorld().playSound(range.center(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
    }


}
