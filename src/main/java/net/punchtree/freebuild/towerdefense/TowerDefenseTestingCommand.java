package net.punchtree.freebuild.towerdefense;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TowerDefenseTestingCommand implements CommandExecutor, TabCompleter {

    // ===== Placing tower functionality =====
    // Right clicking on placeable ground opens a menu for choosing a tower type
    // Upon choosing a tower type, the menu is closed, and the game attempts to place the tower
    //
    // If the tower can be placed (its footprint is unoccupied), the tower is placed
    // When the tower is ticked, it checks if it has any enemies in range
    // If it does, it attacks the furthest-along enemy

    private final TowerDefenseMapManager towerDefenseMapManager;
    private final TowerDefensePlayerManager towerDefensePlayerManager;

    public TowerDefenseTestingCommand(TowerDefenseMapManager towerDefenseMapManager, TowerDefensePlayerManager towerDefensePlayerManager) {
        this.towerDefenseMapManager = towerDefenseMapManager;
        this.towerDefensePlayerManager = towerDefensePlayerManager;
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
        TowerDefenseGame map = towerDefenseMapManager.getMap(args[1]);
        if (map == null) {
            player.sendMessage(ChatColor.RED + "Map not found");
            return true;
        }

        switch (args[0]) {
            case "test-path" -> map.testPaths();
            case "spawn-mob" -> map.spawnMob();
            case "register-me" -> {
                towerDefensePlayerManager.registerPlayer(player, map);
                player.sendMessage(ChatColor.GREEN + "Registered you for map " + map.getName());
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of("test-path", "spawn-mob", "register-me");
    }
}
