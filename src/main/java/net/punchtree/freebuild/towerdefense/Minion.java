package net.punchtree.freebuild.towerdefense;

import com.destroystokyo.paper.ParticleBuilder;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.util.color.PunchTreeColor;
import net.punchtree.util.debugvar.DebugVars;
import net.punchtree.util.particle.ParticleShapes;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

import java.awt.*;

public class Minion {

    private TowerDefenseGame game;
    private final Path path;

    private PathPosition pathPosition;
    private final double speed;
    private final double maxHealth;
    private double health;

    // TODO actually position minions at the offsets, do damage according to range, etc
    private final double offsetX, offsetZ;

    private long poisonedUntil = 0;

    public Minion(TowerDefenseGame game, Path path, double speed, double maxHealth) {
        this.game = game;
        this.path = path;
        this.speed = speed;
        this.pathPosition = new PathPosition(path, 0, 0);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        final double offsetMax = 1;
        this.offsetX = Math.random() * offsetMax * 2 - offsetMax;
        this.offsetZ = Math.random() * offsetMax * 2 - offsetMax;
    }

    public void tick() {
        ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(new PunchTreeColor(new Color(Color.HSBtoRGB((float) (health / maxHealth), 1f, 1f))).getBukkitColor()));
        ParticleShapes.spawnParticle(getLocation());

        pathPosition.advance(speed);

        if (isPoisoned()) {
            damage(DebugVars.getDecimalAsDouble("td:poison_damage", 0.02));
            ParticleShapes.setParticleBuilder(new ParticleBuilder(Particle.REDSTONE).color(0, 255, 200));
            ParticleShapes.spawnParticleLine(getLocation().clone().add(0, 0.5, 0), getLocation().clone().add(0, 1.5, 0), 5);
        }
    }

    private boolean isPoisoned() {
        return System.currentTimeMillis() < poisonedUntil;
    }

    public Location getLocation() {
        return path.getLocation(pathPosition);
    }

    public void damage(double damage) {
        health -= damage;
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

    public void poison(int durationTicks) {
        poisonedUntil = System.currentTimeMillis() + durationTicks * 50L;
    }

}
