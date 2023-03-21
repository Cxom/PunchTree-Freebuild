package net.punchtree.freebuild.player;

import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import net.punchtree.freebuild.datahandling.DatabaseConnection;
import net.punchtree.freebuild.datahandling.MapUtils;
import net.punchtree.freebuild.datahandling.YamlDatabaseConnection;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * The PlayerDataHandler class is responsible for managing the data of a specific player
 * within the context of the PunchTreeFreebuildPlugin. It provides methods for loading,
 * modifying, and saving player data, as well as performing actions once the data is loaded.
 */
public class PlayerDataHandler {
    private static final PunchTreeFreebuildPlugin plugin = PunchTreeFreebuildPlugin.getInstance();
    private static final Executor mainThreadExecutor = Bukkit.getScheduler().getMainThreadExecutor(plugin);
    private final DatabaseConnection playerDataConnection;
    private Map<String, Object> playerData;
    private DataLoadingState dataLoadingState = DataLoadingState.NOT_LOADED;
    private final Queue<Consumer<PlayerDataHandler>> dataLoadedActions = new ConcurrentLinkedQueue<>();

    private enum DataLoadingState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    /**
     * Constructs a PlayerDataHandler with the specified player UUID.
     *
     * @param uuid The UUID of the player whose data is to be managed.
     */
    public PlayerDataHandler(UUID uuid) {
        this.playerDataConnection = new YamlDatabaseConnection("players/" + uuid + ".yml");
    }

    /**
     * Loads player data and then runs the specified action.
     *
     * @param action A Runnable to execute after player data has been loaded.
     */
    private void loadData(Runnable action) {
        dataLoadingState = DataLoadingState.LOADING;

        playerDataConnection.connect(true)
                .thenAcceptAsync(connection -> {
                    Optional<Map<String, Object>> optionalData = connection.read("");
                    playerData = optionalData.orElseGet(LinkedHashMap::new);
                    connection.disconnect();
                    dataLoadingState = DataLoadingState.LOADED;
                }, mainThreadExecutor)
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                })
                .thenRunAsync(action, mainThreadExecutor);
    }


    /**
     * Executes the specified action when the player data is loaded, or adds the action to the queue if the data is still loading.
     *
     * @param action the action to execute when the player data is loaded
     */
    public void withDataLoaded(Consumer<PlayerDataHandler> action) {
        switch (dataLoadingState) {
            case LOADED -> action.accept(this);
            case LOADING -> dataLoadedActions.add(action);
            case NOT_LOADED -> {
                dataLoadedActions.add(action);
                loadData(() -> {
                    while (!dataLoadedActions.isEmpty()) {
                        Consumer<PlayerDataHandler> dataLoadedAction = dataLoadedActions.poll();
                        dataLoadedAction.accept(this);
                    }
                });
            }
        }
    }

    /**
     * Saves the player data to the YAML file.
     */
    public void saveData() {
        Map<String, Object> playerDataCopy = new LinkedHashMap<>(playerData);

        playerDataConnection.connect(true)
                .thenAccept(connection -> {
                    try {
                        connection.upsert("", playerDataCopy);
                        if(connection instanceof YamlDatabaseConnection yamlConnection) {
                            yamlConnection.save();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    } finally {
                        connection.disconnect();
                    }
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }



    /**
     * Retrieves a value from the player data map using the specified table and key.
     * This method should be called inside a {@link #withDataLoaded(Consumer)} action
     * to ensure the player data is loaded and available.
     *
     * @param table The table to look up the data in.
     * @param key The key of the data.
     * @return An Optional containing the value if found, otherwise empty.
     */
    public Optional<Object> getPlayerData(String table, String key) {
        return Optional.ofNullable(MapUtils.getNestedValue(playerData, table, key));
    }

    /**
     * Sets a value in the player data map using the specified table and key.
     * This method should be called inside a {@link #withDataLoaded(Consumer)} action
     * to ensure the player data is loaded and available.
     *
     * @param table The table to store the data in.
     * @param key The key of the data.
     * @param value The value to be stored.
     */
    public void setPlayerData(String table, String key, Object value) {
        MapUtils.setNestedValue(playerData, table, key, value);
    }
}