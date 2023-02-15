package net.punchtree.freebuild.towerdefense.tower;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.towerdefense.Range;
import net.punchtree.freebuild.towerdefense.TowerDefenseGame;
import net.punchtree.freebuild.towerdefense.TowerType;
import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;

public class DotDamageTower implements Tower {
    private TowerDefenseGame game;
    private TowerType type;
    private final Range range;
    private final int ticksBetweenShots = 10;

    public DotDamageTower(TowerDefenseGame game, Block selectedTowerBuildBlock, TowerType type) {
        this.game = game;
        this.type = type;

        Location center = selectedTowerBuildBlock.getLocation().add(0.5, 0, 0.5);
        this.range = new Range(center, 4.5);
    }

    int tickCounter = 0;
    @Override
    public void tick() {
        // AOE damage
        // get the furthest along enemy THAT IS WITHIN the range of this tower
        // then spawn an explosion that damages all enemies within the range of the explosion
        ++tickCounter;
        if ((tickCounter %= ticksBetweenShots) != 0) return;
        game.getMinionsWithinRange(range).forEach(minion -> {
            minion.poison(DebugVars.getInteger("td:poison_damage_duration_ticks", 100));
            minion.getLocation().getWorld().playSound(minion.getLocation(), Sound.ENTITY_HOSTILE_SPLASH, 1, 1);
        });

        net.punchtree.util.particle.ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(Color.WHITE));
        range.draw();
    }
}
