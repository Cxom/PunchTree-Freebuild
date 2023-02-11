package net.punchtree.freebuild.towerdefense;

import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TowerDefenseMap {

    private final Path path;
    private final List<Minion> minions = new ArrayList<>();

    private BukkitTask minionTickTask;

    public TowerDefenseMap(Path path) {
        this.path = path;

        this.minionTickTask = new BukkitRunnable() {
            @Override
            public void run() {
                minions.forEach(Minion::tick);
            }
        }.runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), 0, 1);
    }

    public void testPaths() {
        path.playTestAnimation();
    }

    public void spawnMob() {
        minions.add(new Minion(path, DebugVars.getDecimalAsDouble("td_minion_speed", 0.5)));
    }

    public void onDisable() {
        if (minionTickTask != null) {
            minionTickTask.cancel();
            minionTickTask = null;
        }
    }
}
