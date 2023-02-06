package net.punchtree.freebuild.billiards;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Set;

import static net.punchtree.freebuild.billiards.BilliardsItems.CUE_STICK;

public class BilliardsCommand implements CommandExecutor {

    private final BilliardsManager billiardsManager;

    public BilliardsCommand(BilliardsManager billiardsManager) {
        this.billiardsManager = billiardsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if ( ! (sender instanceof Player player)) return false;

        if (args.length == 0) return false;

        switch (args[0]) {
            case "get-cue-stick" -> player.getInventory().addItem(CUE_STICK);
            case "register-table" -> billiardsManager.attemptToRegisterTable(player);
            case "spawn-cue-ball" -> {
                Set<BilliardTable> nearbyTables = billiardsManager.getNearbyTables(player.getLocation());
                nearbyTables.forEach(BilliardTable::spawnCueBall);
                player.sendMessage(ChatColor.AQUA + "Spawned cue ball!");
            }
            case "shoot-cue-ball" -> {
                Set<BilliardTable> nearbyTables = billiardsManager.getNearbyTables(player.getLocation());
                nearbyTables.forEach(BilliardTable::shootCueBall);
                player.sendMessage(ChatColor.AQUA + "Shot cue ball!");
            }
            case "shoot-multi-ball" -> {
                Set<BilliardTable> nearbyTables = billiardsManager.getNearbyTables(player.getLocation());
                nearbyTables.forEach(BilliardTable::shootMultiBall);
                player.sendMessage(ChatColor.AQUA + "Shot multi ball!");
            }
            case "rack" -> {
                Set<BilliardTable> nearbyTables = billiardsManager.getNearbyTables(player.getLocation());
                nearbyTables.forEach(BilliardTable::rackEightBall);
                player.sendMessage(ChatColor.AQUA + "Freshly racked!");
            }
            default -> player.sendMessage("Subcommand not recognized");
        }

        return true;
    }
}
