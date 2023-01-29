package net.punchtree.freebuild;

import net.punchtree.freebuild.claiming.commands.ClaimTestingCommand;
import net.punchtree.freebuild.commands.BlocksCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PunchTreeFreebuildPlugin extends JavaPlugin {
    //Sock was here.
    @Override
    public void onEnable() {
        BlocksCommand blocksCommand = new BlocksCommand();
        getCommand("blocks").setExecutor(blocksCommand);
        Bukkit.getPluginManager().registerEvents(blocksCommand, this);

        getCommand("claimtest").setExecutor(new ClaimTestingCommand());
    }

}
