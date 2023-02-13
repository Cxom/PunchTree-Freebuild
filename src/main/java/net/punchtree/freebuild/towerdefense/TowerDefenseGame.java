package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TowerDefenseGame {

    private final String name;
    private final Path path;
    private final List<Minion> minions = new ArrayList<>();

    private BukkitTask minionTickTask;

    public TowerDefenseGame(String string, Path path) {
        this.name = string;
        this.path = path;

        this.minionTickTask = new BukkitRunnable() {
            @Override
            public void run() {
                minions.forEach(Minion::tick);
            }
        }.runTaskTimer(PunchTreeFreebuildPlugin.getInstance(), 0, 1);
    }

    public String getName() {
        return name;
    }

    public void testPaths() {
        path.playTestAnimation();
    }

    public void spawnMob() {
        minions.add(new Minion(path, DebugVars.getDecimalAsDouble("td_minion_speed", 0.5)));
    }

    public void attemptPlaceTower(TowerDefensePlayer tdPlayer, TowerType basic, Location selectedTowerBuildLocation) {
        // Take the given block, then ask the tower type for its footprint (list of blocks)
        // For each block in the footprint, check if the block above (the bottom of the tower) is air
        // If any of the blocks in the footprint are not air, then the tower cannot be placed
        // Otherwise, place the tower, and append it to a list of towers in the game

        if (!isPlaceable(basic, selectedTowerBuildLocation)) {
            tdPlayer.getPlayer().sendMessage(ChatColor.RED + "Cannot place tower here!");
            return;
        }

        // It's placeable!
        tdPlayer.getPlayer().sendMessage(Component.text("Placing a tower of type ").append(basic.getName()).append(Component.text("!")));
        for(Block block : basic.getFootprint(selectedTowerBuildLocation)) {
            block.getRelative(BlockFace.UP).setType(basic.getIcon().getType());
        }
    }

    private boolean isPlaceable(TowerType basic, Location selectedTowerBuildLocation) {
        for(Block block : basic.getFootprint(selectedTowerBuildLocation)) {
            if (block.getType() != TowerBuildingListener.TOWER_PLACEABLE_AREA_MATERIAL || !block.getRelative(0, 1, 0).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void onDisable() {
        if (minionTickTask != null) {
            minionTickTask.cancel();
            minionTickTask = null;
        }
    }
}
