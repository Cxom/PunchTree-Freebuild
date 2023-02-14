package net.punchtree.freebuild.towerdefense;

import net.kyori.adventure.text.Component;
import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.towerdefense.tower.AoeDamageTower;
import net.punchtree.freebuild.towerdefense.tower.DirectDamageTower;
import net.punchtree.freebuild.towerdefense.tower.Tower;
import net.punchtree.util.debugvar.DebugVars;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TowerDefenseGame {

    private final String name;
    private final Path path;
    private final List<Minion> minions = new ArrayList<>();
    private final List<Tower> towers = new ArrayList<>();

    private BukkitTask tickTask;

    public TowerDefenseGame(String string, Path path) {
        this.name = string;
        this.path = path;

        this.tickTask = new BukkitRunnable() {
            @Override
            public void run() {
                minions.forEach(Minion::tick);
                towers.forEach(Tower::tick);
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
        minions.add(new Minion(this, path, DebugVars.getDecimalAsDouble("td_minion_speed", 0.1), 10));
    }

    public void attemptPlaceTower(TowerDefensePlayer tdPlayer, TowerType type, Block selectedTowerBuildLocation) {
        // Take the given block, then ask the tower type for its footprint (list of blocks)
        // For each block in the footprint, check if the block above (the bottom of the tower) is air
        // If any of the blocks in the footprint are not air, then the tower cannot be placed
        // Otherwise, place the tower, and append it to a list of towers in the game

        if (!isPlaceable(type, selectedTowerBuildLocation)) {
            tdPlayer.getPlayer().sendMessage(ChatColor.RED + "Cannot place tower here!");
            return;
        }

        // It's placeable!
        tdPlayer.getPlayer().sendMessage(Component.text("Placing a tower of type ").append(type.getName()).append(Component.text("!")));
        for(Block block : type.getFootprint(selectedTowerBuildLocation)) {
            block.getRelative(BlockFace.UP).setType(type.getIcon().getType());
        }

        if (type == TowerType.DIRECT_DAMAGE) {
            DirectDamageTower tower = new DirectDamageTower(this, selectedTowerBuildLocation, type);
            towers.add(tower);
        } else if (type == TowerType.AOE_DAMAGE) {
            AoeDamageTower tower = new AoeDamageTower(this, selectedTowerBuildLocation, type);
            towers.add(tower);
        }
    }

    private boolean isPlaceable(TowerType basic, Block selectedTowerBuildLocation) {
        for(Block block : basic.getFootprint(selectedTowerBuildLocation)) {
            if (block.getType() != TowerBuildingListener.TOWER_PLACEABLE_AREA_MATERIAL || !block.getRelative(0, 1, 0).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void onDisable() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
    }

    public Optional<Minion> getFurthestMinionWithinRange(Range range) {
        return minions.stream()
                .filter(minion -> range.contains(minion.getLocation()))
                .findFirst();
    }

    public void removeMinion(Minion minion) {
        minions.remove(minion);
    }

    // TODO optimize!!!!!
    public List<Minion> getMinionsWithinRange(Range range) {
        return minions.stream()
                .filter(minion -> range.contains(minion.getLocation()))
                .toList();
    }
}
