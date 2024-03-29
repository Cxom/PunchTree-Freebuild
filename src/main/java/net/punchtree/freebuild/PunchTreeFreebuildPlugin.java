package net.punchtree.freebuild;

import net.punchtree.freebuild.afk.AfkCommand;
import net.punchtree.freebuild.afk.AfkPlayerListener;
import net.punchtree.freebuild.afk.RosterManager;
import net.punchtree.freebuild.ambientvoting.AmbientVoteCommand;
import net.punchtree.freebuild.ambientvoting.NightTimeRunnable;
import net.punchtree.freebuild.arbor.Arbor;
import net.punchtree.freebuild.arbor.ArborOnAsyncChat;
import net.punchtree.freebuild.arbor.ArborOnPlayerJoin;
import net.punchtree.freebuild.arbor.ArborOnPlayerLeave;
import net.punchtree.freebuild.billiards.BilliardsCommand;
import net.punchtree.freebuild.billiards.BilliardsManager;
import net.punchtree.freebuild.billiards.BilliardsShootListener;
import net.punchtree.freebuild.bossfight.WitherFightManager;
import net.punchtree.freebuild.claiming.commands.ClaimTestingCommand;
import net.punchtree.freebuild.claiming.commands.ClaimTestingRegionIndicator;
import net.punchtree.freebuild.commands.AdvancementsCommand;
import net.punchtree.freebuild.commands.BlocksCommand;
import net.punchtree.freebuild.datahandling.DatabaseConnection;
import net.punchtree.freebuild.datahandling.IODispatcher;
import net.punchtree.freebuild.datahandling.YamlDatabaseConnection;
import net.punchtree.freebuild.dimension.NetherPortalListener;
import net.punchtree.freebuild.heartsigns.HeartSignListener;
import net.punchtree.freebuild.parkour.ParkourListener;
import net.punchtree.freebuild.player.PtfbPlayerOnPlayerJoin;
import net.punchtree.freebuild.player.PtfbPlayerOnPlayerQuit;
import net.punchtree.freebuild.playingcards.PlayingCardCommands;
import net.punchtree.freebuild.playingcards.PlayingCardInteractListener;
import net.punchtree.freebuild.towerdefense.*;
import net.punchtree.freebuild.towerdefense.tower.TowerDefenseHotbarUiListener;
import net.punchtree.freebuild.waterparks.SlideManager;
import net.punchtree.freebuild.waterparks.SlideTestingCommand;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

public class PunchTreeFreebuildPlugin extends JavaPlugin {

    private static PunchTreeFreebuildPlugin instance;
    private TowerDefenseMapManager towerDefenseMapManager;
    private TowerDefensePlayerManager towerDefensePlayerManager;
    private WitherFightManager witherFightManager;
    private SlideManager slideManager;
    private ClaimTestingRegionIndicator claimTestingRegionIndicator;

    public static PunchTreeFreebuildPlugin getInstance() {
        return instance;
    }
    private BlocksCommand blocksCommand;
    private AmbientVoteCommand ambientVoteCommand;
    private NightTimeRunnable nightTimeRunnable;
    private BilliardsManager billiardsManager;
    private DatabaseConnection configConnection;
    private static Arbor arbor;
    private static IODispatcher ioDispatcher;

    public static Arbor getArbor() {
        return arbor;
    }
    public static IODispatcher getIODispatcher() {
        return ioDispatcher;
    }

    // TODO confirm this fires before WorldGuard is enabled (if not, does the STARTUP property need to be changed in the plugin.yml?)
    @Override
    public void onLoad() {
        ClaimTestingCommand.registerCustomWorldguardFlags();
    }

    @Override
    public void onEnable() {
        instance = this;

        billiardsManager = new BilliardsManager();
        blocksCommand = new BlocksCommand();
        ambientVoteCommand = new AmbientVoteCommand();

        nightTimeRunnable = new NightTimeRunnable(Bukkit.getWorld("world"));
        nightTimeRunnable.scheduleRepeatingTaskForTime(13000L);
//        witherFightManager = new WitherFightManager();

        slideManager = new SlideManager();

        ioDispatcher = new IODispatcher();
        configConnection = new YamlDatabaseConnection("config.yml");
        loadConfig();

        setCommandExecutors();

        registerEvents();

        initializeClaimTesting();
        initializeTowerDefense();
    }

    private void setCommandExecutors() {
        getCommand("blocks").setExecutor(blocksCommand);
        getCommand("vskip").setExecutor(ambientVoteCommand);
        getCommand("billiards").setExecutor(new BilliardsCommand(billiardsManager));
        getCommand("towerdefense").setExecutor(new TowerDefenseTestingCommand(towerDefenseMapManager, towerDefensePlayerManager));
        getCommand("afk").setExecutor(new AfkCommand());
        getCommand("playingcards").setExecutor(new PlayingCardCommands());
        getCommand("advancements").setExecutor(new AdvancementsCommand());
        getCommand("slide").setExecutor(new SlideTestingCommand(slideManager));
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
        Bukkit.getPluginManager().registerEvents(new PlayingCardInteractListener(), this);
        Bukkit.getPluginManager().registerEvents(new NetherPortalListener(), this);
//        Bukkit.getPluginManager().registerEvents(witherFightManager, this);
        Bukkit.getPluginManager().registerEvents(new ArborOnAsyncChat(), this);
        Bukkit.getPluginManager().registerEvents(new ArborOnPlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new ArborOnPlayerLeave(), this);
        Bukkit.getPluginManager().registerEvents(new PtfbPlayerOnPlayerJoin(), this);
        Bukkit.getPluginManager().registerEvents(new PtfbPlayerOnPlayerQuit(), this);
    }

    private void initializeClaimTesting() {
        claimTestingRegionIndicator = new ClaimTestingRegionIndicator();
        Bukkit.getPluginManager().registerEvents(claimTestingRegionIndicator, this);
        getCommand("claimtest").setExecutor(new ClaimTestingCommand(claimTestingRegionIndicator));
    }

    private void initializeTowerDefense() {
        towerDefenseMapManager = new TowerDefenseMapManager();
        towerDefensePlayerManager = new TowerDefensePlayerManager();
        TowerDefenseTestingCommand towerDefenseTestingCommand = new TowerDefenseTestingCommand(towerDefenseMapManager, towerDefensePlayerManager);
        getCommand("towerdefense").setExecutor(towerDefenseTestingCommand);
        getCommand("towerdefense").setTabCompleter(towerDefenseTestingCommand);
        Bukkit.getPluginManager().registerEvents(new TowerBuildingListener(towerDefensePlayerManager), this);
        Bukkit.getPluginManager().registerEvents(new TowerDefenseQuitListener(towerDefensePlayerManager), this);
        Bukkit.getPluginManager().registerEvents(new TowerDefenseHotbarUiListener(towerDefensePlayerManager), this);
        Bukkit.getPluginManager().registerEvents(new AfkPlayerListener(), this);
    }

    @Override
    public void onDisable() {
        billiardsManager.onDisable();
        ambientVoteCommand.cancelWeatherVote();
        nightTimeRunnable.cancel();
        nightTimeRunnable = null;
        towerDefenseMapManager.onDisable();
        AfkCommand.cooldownPruningTask.cancel();
        AfkPlayerListener.autoAfkTask.cancel();
        if(Bukkit.getScoreboardManager().getMainScoreboard().getTeam("afk") != null) {
            Bukkit.getScoreboardManager().getMainScoreboard().getTeam("afk").unregister();
        }
        RosterManager.getRoster("afk").wipeRoster();
//        witherFightManager.onDisable();
        slideManager.onDisable();
        arbor.stop();
        ioDispatcher.shutdown();
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

    private void loadConfig() {
        configConnection.connect(true).thenAccept(connection -> {
            Optional<Map<String, Object>> discord = connection.read("discord");
            if (discord.isPresent()) {
                Map<String, Object> discordMap = discord.get();
                String discordToken = (String) discordMap.get("token");
                String channelID = (String) discordMap.get("channelID");
                arbor = new Arbor(discordToken, channelID, Executors.newSingleThreadExecutor());
                arbor.start();
            } else {
                Bukkit.getLogger().severe("Discord configuration not found in config.yml");
            }

            connection.disconnect();
        });
    }
}
