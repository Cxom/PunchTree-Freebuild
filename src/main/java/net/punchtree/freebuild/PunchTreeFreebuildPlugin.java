package net.punchtree.freebuild;

import net.punchtree.freebuild.billiards.BilliardsCommand;
import net.punchtree.freebuild.billiards.BilliardsManager;
import net.punchtree.freebuild.billiards.BilliardsShootListener;
import net.punchtree.freebuild.claiming.commands.ClaimTestingCommand;
import net.punchtree.freebuild.commands.BlocksCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PunchTreeFreebuildPlugin extends JavaPlugin {

    private static PunchTreeFreebuildPlugin instance;
    public static PunchTreeFreebuildPlugin getInstance() {
        return instance;
    }

    private BlocksCommand blocksCommand;
    private BilliardsManager billiardsManager;

    //Sock was here.
    @Override
    public void onEnable() {
        instance = this;

        billiardsManager = new BilliardsManager();
        blocksCommand = new BlocksCommand();

        setCommandExecutors();

        registerEvents();
    }

    private void setCommandExecutors() {
        getCommand("blocks").setExecutor(blocksCommand);
        getCommand("claimtest").setExecutor(new ClaimTestingCommand());
        getCommand("billiards").setExecutor(new BilliardsCommand(billiardsManager));
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(blocksCommand, this);
        Bukkit.getPluginManager().registerEvents(new BilliardsShootListener(billiardsManager), this);
        Bukkit.getPluginManager().registerEvents(new JebSheepGiveFreeDye(), this);
    }

    @Override
    public void onDisable() {
        billiardsManager.onDisable();
    }
}
