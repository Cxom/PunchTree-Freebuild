package net.punchtree.freebuild.towerdefense;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TowerDefenseTestingCommand implements CommandExecutor {

    private final TowerDefenseMapManager towerDefenseMapManager;

    public TowerDefenseTestingCommand(TowerDefenseMapManager towerDefenseMapManager) {
        this.towerDefenseMapManager = towerDefenseMapManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( ! (sender instanceof Player player)) {
            return false;
        }

        /*
         * Most basic form of tower defense
         * A mob spawns, walks a set path, runs into a tower
         * stops and attacks the tower
         * if the tower hits zero health, the game is over
         */

        if (args.length < 2) return false;
        TowerDefenseMap map = towerDefenseMapManager.getMap(args[1]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Map not found");
            return true;
        }

        switch (args[0]) {
            case "test-path" -> map.testPaths();
            case "spawn-mob" -> map.spawnMob();
        }

        return true;
    }

}
