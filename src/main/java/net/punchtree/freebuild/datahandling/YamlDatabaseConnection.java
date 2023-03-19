package net.punchtree.freebuild.datahandling;

import net.punchtree.freebuild.PunchTreeFreebuildPlugin;
import org.bukkit.Bukkit;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * A class for connecting to, reading from, and writing to a YAML database.
 * This class implements the DatabaseConnection interface and provides
 * methods for creating, reading, updating, and deleting tables within
 * a YAML file.
 */
public class YamlDatabaseConnection implements DatabaseConnection {
    private Map<String, Object> data;
    private final Path filePath;
    private final Yaml yaml;
    private static final IODispatcher ioDispatcher = PunchTreeFreebuildPlugin.getIODispatcher();
    private boolean isDisconnected;
    private static final Logger LOGGER = Bukkit.getLogger();

    /**
     * Constructs a new YamlDatabaseConnection with the specified file path.
     *
     * @param filePath The path to the YAML file relative to the plugin's data folder.
     */
    public YamlDatabaseConnection(String filePath) {
        // Create a DumperOptions object to control the output format
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Use block style for lists and maps
        options.setPrettyFlow(true); // Indent the output for readability

        this.yaml = new Yaml(options);

        this.filePath = Path.of(PunchTreeFreebuildPlugin.getInstance().getDataFolder().getPath(), filePath);
        this.isDisconnected = true;
    }

    /**
     * Connects to the YAML database and loads the data from the file.
     * If an exception occurs during the execution of the asynchronous task,
     * it will be caught and logged to the console.
     *
     * @param forced If true, creates the file if it doesn't exist, and
     *               allows empty files (an empty map will be used as data).
     *               If false, throws an exception if the file is empty.
     * @return A CompletableFuture that resolves to this YamlDatabaseConnection
     *         instance once the connection is established and the data is loaded.
     *         If an exception occurs, the CompletableFuture resolves to null.
     */
    @Override
    public CompletableFuture<YamlDatabaseConnection> connect(boolean forced) {
        return ioDispatcher.submitYamlTask(() -> {
            try {
                ensureFileExistsIfForced(forced);
            } catch (IOException e) {
                throw new RuntimeException("Failed to ensure file creation", e);
            }

            try (Reader reader = new FileReader(filePath.toFile())) {
                Map<String, Object> loadedData = yaml.load(reader);
                handleLoadedData(forced, loadedData);

            } catch (IOException e) {
                throw new RuntimeException("Failed to load YAML file", e);
            }
            isDisconnected = false;
            return this;
        }).exceptionally(ex -> {
            LOGGER.severe("Error while connecting to YAML database: " + filePath);
            LOGGER.severe("Exception: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Disconnects from the YAML database, saving any changes made to the
     * YAML file. This method should always be called after finishing operations
     * with the YamlDatabaseConnection to ensure that data is saved properly.
     */
    @Override
    public void disconnect() {
        ioDispatcher.submitYamlTask(() -> {
            try (Writer writer = new FileWriter(filePath.toFile())) {
                yaml.dump(data, writer);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save YAML file", e);
            }
            data = null;
            isDisconnected = true;
            return this;
        }).exceptionally(ex -> {
            LOGGER.severe("Error while disconnecting from YAML database: " + filePath);
            LOGGER.severe("Exception: " + ex.getMessage());
            return null;
        });
    }

    /**
     * Creates a new table in the YAML database and populates it with the
     * given values. The table is represented as a nested map within the
     * YAML file. If the table already exists, its contents will be updated
     * with the provided values.
     *
     * @param table  The path to the table to be created. The path is a
     *               dot-separated string representing the nested structure
     *               of the table (e.g., "parent.child").
     * @param values The values to populate the new table with. The keys in
     *               the provided map represent the fields in the table.
     * @return The created table as a map.
     * @throws PathCreationFailedException If the path to the table could not be created.
     * @throws IllegalStateException        If the database connection is disconnected.
     */
    @Override
    public Map<String, Object> create(String table, Map<String, Object> values) throws PathCreationFailedException, IllegalStateException {
        checkDisconnected();
        Map<String, Object> locatedMap = traverseMapByPath(data, table, true);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to create the path: " + table);
        }
        locatedMap.putAll(values);
        return locatedMap;
    }

    /**
     * Reads the contents of a table in the YAML database. If the table
     * does not exist, an empty Optional is returned.
     *
     * @param table The path to the table to be read. The path is a
     *              dot-separated string representing the nested structure
     *              of the table (e.g., "parent.child").
     * @return An Optional containing the table contents as a map if the table exists, or an empty Optional if the table does not exist.
     * @throws IllegalStateException If the database connection is disconnected.
     **/
    @Override
    public Optional<Map<String, Object>> read(String table) throws IllegalStateException {
        checkDisconnected();
        return Optional.ofNullable(traverseMapByPath(data, table, false));
    }

    /**
     * Updates the contents of a table in the YAML database with the given values.
     * If the table does not exist, a PathCreationFailedException is thrown.
     *
     * @param table  The path to the table to be updated. The path is a
     *               dot-separated string representing the nested structure
     *               of the table (e.g., "parent.child").
     * @param values The values to update the table with. The keys in
     *               the provided map represent the fields in the table.
     * @return The updated table as a map.
     * @throws PathCreationFailedException If the path to the table could not be found.
     * @throws IllegalStateException        If the database connection is disconnected.
     */
    @Override
    public Map<String, Object> update(String table, Map<String, Object> values) throws PathCreationFailedException, IllegalStateException {
        checkDisconnected();
        Map<String, Object> locatedMap = traverseMapByPath(data, table, false);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to find the path: " + table);
        }
        locatedMap.putAll(values);
        return locatedMap;
    }

    /**
     * Deletes a table from the YAML database by clearing its contents.
     * If the table does not exist, a PathCreationFailedException is thrown.
     *
     * @param table The path to the table to be deleted. The path is a
     *              dot-separated string representing the nested structure
     *              of the table (e.g., "parent.child").
     * @return The deleted table as a map containing the original data.
     * @throws PathCreationFailedException If the path to the table could not be found.
     * @throws IllegalStateException        If the database connection is disconnected.
     */
    @Override
    public Map<String, Object> delete(String table) throws PathCreationFailedException, IllegalStateException {
        checkDisconnected();
        Map<String, Object> locatedMap = traverseMapByPath(data, table, false);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to find the path: " + table);
        }
        Map<String, Object> clonedMap = new LinkedHashMap<>(locatedMap);
        locatedMap.clear();
        return clonedMap;
    }

    /**
     * Upserts (inserts or updates) the given key-value pairs in the specified table.
     * If the table does not exist, it will be created. If the table exists,
     * the provided key-value pairs will be updated or added to the table.
     *
     * @param table  the table to perform the upsert operation on
     * @param values a map containing the key-value pairs to be inserted or updated
     * @return the updated table after performing the upsert operation
     * @throws PathCreationFailedException if the path creation fails
     */
    @Override
    public Map<String, Object> upsert(String table, Map<String, Object> values) throws PathCreationFailedException {
        checkDisconnected();
        Map<String, Object> locatedMap = traverseMapByPath(data, table, true);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to create the path: " + table);
        }
        locatedMap.putAll(values);
        return locatedMap;
    }



    /**
     * Traverses the YAML database map using the specified path.
     * If the forcePath parameter is true, it creates the necessary structure
     * along the path if it does not exist.
     *
     * @param map      The map to traverse.
     * @param path     The dot-separated path to the desired table (e.g., "parent.child").
     * @param forcePath If true, creates the necessary structure along the path if it does not exist.
     * @return The map located at the specified path or null if the path does not exist and forcePath is false.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> traverseMapByPath(Map<String, Object> map, String path, boolean forcePath) {
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            if (i == keys.length - 1) {
                if (forcePath && !currentMap.containsKey(key)) {
                    currentMap.put(key, new LinkedHashMap<String, Object>());
                }
                return (Map<String, Object>) currentMap.get(key);
            }

            currentMap = getNextMap(currentMap, key, forcePath);

            if (currentMap == null) {
                return null;
            }
        }
        return currentMap;
    }


    /**
     * Retrieves or creates the next map in the path based on the current map,
     * key, and forcePath flag.
     *
     * @param currentMap The current map being traversed.
     * @param key        The key for the next map.
     * @param forcePath  If true, creates a new map for the key if it does not exist.
     * @return The next map in the path or null if the key does not exist and forcePath is false.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getNextMap(Map<String, Object> currentMap, String key, boolean forcePath) {
        Object value = currentMap.get(key);
        if (currentMap.containsKey(key)) {
            if (value instanceof Map) {
                return (Map<String, Object>) value;
            }
        }

        if (forcePath) {
            return createNewMapAndPutInCurrentMap(currentMap, key);
        }

        return null;
    }

    /**
     * Creates a new empty map and puts it in the current map with the specified key.
     *
     * @param currentMap The current map being updated.
     * @param key        The key for the new map.
     * @return The newly created map.
     */
    private Map<String, Object> createNewMapAndPutInCurrentMap(Map<String, Object> currentMap, String key) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        currentMap.put(key, newMap);
        return newMap;
    }

    /**
     * Checks if the database connection is disconnected. If it is, throws an
     * IllegalStateException with a message indicating that the operation
     * cannot be performed on a disconnected YamlDatabaseConnection.
     *
     * @throws IllegalStateException If the database connection is disconnected.
     */
    private void checkDisconnected() {
        if (isDisconnected) {
            throw new IllegalStateException("Cannot perform operation on a disconnected YamlDatabaseConnection");
        }
    }

    /**
     * Ensures the YAML file exists if the 'forced' flag is true.
     * Creates the file and its parent directories if they do not exist.
     *
     * @param forced indicates whether to create the file and directories if they do not exist
     * @throws IOException if there's an issue creating the file or directories
     */
    private void ensureFileExistsIfForced(boolean forced) throws IOException {
        if (forced) {
            Files.createDirectories(filePath.getParent());
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
        }
    }

    /**
     * Handles the loaded data from the YAML file.
     * If the loaded data is null and 'forced' is true, it creates a new empty LinkedHashMap.
     *
     * @param forced     indicates whether to create an empty LinkedHashMap if the loaded data is null
     * @param loadedData the data loaded from the YAML file, can be null
     */
    private void handleLoadedData(boolean forced, Map<String, Object> loadedData) {
        if (loadedData == null) {
            if (!forced) {
                throw new RuntimeException("YAML file is empty: " + filePath);
            }
            data = new LinkedHashMap<>();
        } else {
            data = loadedData;
        }
    }

    /**
     * A custom exception class used for cases when the path creation fails
     * while attempting to create or locate a table within the YAML database.
     */
    private static class PathCreationFailedException extends Exception {
        /**
         * Constructs a new PathCreationFailedException with the specified detail message.
         *
         * @param message The detail message, which is saved for later retrieval by the Throwable.getMessage() method.
         */
        public PathCreationFailedException(String message) {
            super(message);
        }
    }
}
