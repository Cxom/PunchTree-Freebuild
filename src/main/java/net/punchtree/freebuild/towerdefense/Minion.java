package net.punchtree.freebuild.towerdefense;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class Minion {

    private TowerDefenseGame game;
    private final Path path;

    private PathPosition pathPosition;
    private final double speed;
    private final double maxHealth;
    private double health;

    public Minion(TowerDefenseGame game, Path path, double speed, double maxHealth) {
        this.game = game;
        this.path = path;
        this.speed = speed;
        this.pathPosition = new PathPosition(path, 0, 0);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public void tick() {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(255, 50, 50));
        ParticleShapes.spawnParticle(getLocation());

        pathPosition.advance(speed);
    }

    Location getLocation() {
        return path.getLocation(pathPosition);
    }

    public void damage(int i) {
        health -= i;
        if (health <= 0) {
            die();
        }
    }

    private void die() {
        game.removeMinion(this);
        final Location deathLocation = getLocation();
        deathLocation.getWorld().playSound(deathLocation, Sound.ENTITY_GENERIC_DEATH, 1, 1);
        new BukkitRunnable(){
            int steps = 0;
            public void run() {
                double radius = (steps + 1) * .5;
                net.punchtree.freebuild.util.particle.ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(255, 50, 50));
                net.punchtree.freebuild.util.particle.ParticleShapes.drawCircle(deathLocation, radius, (int) (radius * radius * Math.PI * 3));
                ++steps;
                if (steps > 3) {
                    this.cancel();
                }
            }
        }.runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), 0, 5);
    }
}
