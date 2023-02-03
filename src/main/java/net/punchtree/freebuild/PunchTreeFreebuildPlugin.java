package net.punchtree.freebuild;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
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

    private IntegerFlag NUMBER_OF_CLAIMS_FLAG;

    // TODO confirm this fires before WorldGuard is enabled (if not, does the STARTUP property need to be changed in the plugin.yml?)
    @Override
    public void onLoad() {
        registerCustomWorldguardFlags();
    }

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

    private void registerCustomWorldguardFlags() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        try {
            NUMBER_OF_CLAIMS_FLAG = new IntegerFlag("number-of-claims");
            flagRegistry.register(NUMBER_OF_CLAIMS_FLAG);
        } catch (FlagConflictException fce) {
            Bukkit.getLogger().severe("Could not register our custom flag!");
        }
    }

    @Override
    public void onDisable() {
        billiardsManager.onDisable();
    }
}
