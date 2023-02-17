package net.punchtree.freebuild;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import net.punchtree.freebuild.ambientvoting.NightTimeRunnable;
import net.punchtree.freebuild.billiards.BilliardsCommand;
import net.punchtree.freebuild.billiards.BilliardsManager;
import net.punchtree.freebuild.billiards.BilliardsShootListener;
import net.punchtree.freebuild.claiming.commands.ClaimTestingCommand;
import net.punchtree.freebuild.commands.AmbientVoteCommand;
import net.punchtree.freebuild.commands.BlocksCommand;
import net.punchtree.freebuild.heartsigns.HeartSignListener;
import net.punchtree.freebuild.parkour.ParkourListener;
import net.punchtree.freebuild.towerdefense.*;
import net.punchtree.freebuild.towerdefense.tower.TowerDefenseHotbarUiListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PunchTreeFreebuildPlugin extends JavaPlugin {

    private static PunchTreeFreebuildPlugin instance;
    private TowerDefenseMapManager towerDefenseMapManager;
    private TowerDefensePlayerManager towerDefensePlayerManager;

    public static PunchTreeFreebuildPlugin getInstance() {
        return instance;
    }
    private BlocksCommand blocksCommand;
    private AmbientVoteCommand ambientVoteCommand;
    private NightTimeRunnable nightTimeRunnable;
    private BilliardsManager billiardsManager;

    private IntegerFlag NUMBER_OF_CLAIMS_FLAG;

    // TODO confirm this fires before WorldGuard is enabled (if not, does the STARTUP property need to be changed in the plugin.yml?)
    @Override
    public void onLoad() {
        registerCustomWorldguardFlags();
    }

    @Override
    public void onEnable() {
        instance = this;

        billiardsManager = new BilliardsManager();
        blocksCommand = new BlocksCommand();
        ambientVoteCommand = new AmbientVoteCommand();

        nightTimeRunnable = new NightTimeRunnable(Bukkit.getWorld("world"));
        nightTimeRunnable.scheduleRepeatingTaskForTime(13000L);

        setCommandExecutors();

        registerEvents();

        initializeTowerDefense();
    }

    private void setCommandExecutors() {
        getCommand("blocks").setExecutor(blocksCommand);
        getCommand("vskip").setExecutor(ambientVoteCommand);
        getCommand("claimtest").setExecutor(new ClaimTestingCommand());
        getCommand("billiards").setExecutor(new BilliardsCommand(billiardsManager));
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(blocksCommand, this);
        Bukkit.getPluginManager().registerEvents(new BilliardsShootListener(billiardsManager), this);
        Bukkit.getPluginManager().registerEvents(new JebSheepGiveFreeDye(), this);
        Bukkit.getPluginManager().registerEvents(ambientVoteCommand, this);
        Bukkit.getPluginManager().registerEvents(new ParkourListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnCobblestoneForm(), this);
        Bukkit.getPluginManager().registerEvents(new HeartSignListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnPlayerDamageEntity(), this);
    }

    private void initializeTowerDefense() {
        towerDefenseMapManager = new TowerDefenseMapManager();
        towerDefensePlayerManager = new TowerDefensePlayerManager();
        getCommand("towerdefense").setExecutor(new TowerDefenseTestingCommand(towerDefenseMapManager, towerDefensePlayerManager));
        Bukkit.getPluginManager().registerEvents(new TowerBuildingListener(towerDefensePlayerManager), this);
        Bukkit.getPluginManager().registerEvents(new TowerDefenseQuitListener(towerDefensePlayerManager), this);
        Bukkit.getPluginManager().registerEvents(new TowerDefenseHotbarUiListener(towerDefensePlayerManager), this);
    }

    private void registerCustomWorldguardFlags() {
        FlagRegistry flagRegistry = WorldGuard.getInstance().getFlagRegistry();
        String NUMBER_OF_CLAIMS_FLAG_NAME = "number-of-claims";
        try {
            NUMBER_OF_CLAIMS_FLAG = new IntegerFlag(NUMBER_OF_CLAIMS_FLAG_NAME);
            flagRegistry.register(NUMBER_OF_CLAIMS_FLAG);
        } catch (FlagConflictException fce) {
            Bukkit.getLogger().severe("Could not register our custom flag!");
        } catch (IllegalStateException ise) {
            // the plugin is being loaded after worldguard
            NUMBER_OF_CLAIMS_FLAG = (IntegerFlag) flagRegistry.get(NUMBER_OF_CLAIMS_FLAG_NAME);
        }
    }

    @Override
    public void onDisable() {
        billiardsManager.onDisable();
        ambientVoteCommand.cancelWeatherVote();
        nightTimeRunnable.cancel();
        nightTimeRunnable = null;
        towerDefenseMapManager.onDisable();
    }

    public void setNightTimeRunnable(NightTimeRunnable nightTimeRunnable, long startTime) {
        if(this.nightTimeRunnable != null && !this.nightTimeRunnable.isCancelled()){
            this.nightTimeRunnable.cancel();
        }
        this.nightTimeRunnable = nightTimeRunnable;
        this.nightTimeRunnable.scheduleRepeatingTaskForTime(startTime);
    }

    public NightTimeRunnable getNightTimeRunnable() {
        return nightTimeRunnable;
    }
}
