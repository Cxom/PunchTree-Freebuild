package net.punchtree.freebuild.towerdefense;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

public class TowerDefenseMapManager {

    private final Map<String, TowerDefenseMap> maps = new HashMap<>();

    public TowerDefenseMapManager() {
        World world = Bukkit.getWorld("world");
        Location pathPoint1 = new Location(world, -86.5, 17.00, -1672.5);
        Location pathPoint2 = new Location(world, -72.5, 17.00, -1672.5);
        Location pathPoint3 = new Location(world, -72.5, 17.00, -1686.5);
        Location pathPoint4 = new Location(world, -59.5, 17.00, -1686.5);
        Path path = new Path(new Location[]{pathPoint1, pathPoint2, pathPoint3, pathPoint4});
        maps.put("test", new TowerDefenseMap(path));
    }

    public TowerDefenseMap getMap(String mapName) {
        return maps.get(mapName);
    }

    public void onDisable() {
        maps.values().forEach(TowerDefenseMap::onDisable);
    }
}
