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
 * A class representing a connection to a YAML-based database. This class implements the
 * {@link DatabaseConnection} interface, providing methods to create, read, update, and delete
 * data from the database.
 * <p>
 * The data is stored in a YAML file on disk, and is loaded and saved using the SnakeYAML library.
 * The file path can be specified when creating a new instance of this class, and the data is stored
 * as a map of string keys to object values.
 * <p>
 * This class is thread-safe and supports asynchronous operations. Connections can be established
 * and closed using the {@link #connect(boolean)} and {@link #disconnect()} methods, respectively.
 * Once connected, data can be manipulated using the various CRUD methods provided by the
 * {@link DatabaseConnection} interface.
 * <p>
 * Example usage:
 * <pre>{@code
 * YamlDatabaseConnection connection = new YamlDatabaseConnection("data/my_database.yml");
 * connection.connect(false).thenAccept(conn -> {
 *     conn.upsert("users.johndoe", Map.of("name", "John Doe", "age", 42));
 *     conn.update("users.johndoe", Map.of("age", 43, "email", "john.doe@example.com"));
 *     conn.delete("users.johndoe");
 *
 *     Optional<Map<String, Object>> result = conn.read("users.johndoe");
 *     if (!result.isPresent()) {
 *         conn.upsert("users.janedoe", Map.of("name", "Jane Doe", "age", 27));
 *     }
 *
 *     conn.save().thenRun(() -> System.out.println("Data saved"));
 *     conn.disconnect();
 * });
 * }</pre>
 */
public class YamlDatabaseConnection implements DatabaseConnection {
    private Map<String, Object> data;
    private final Path filePath;
    private final Yaml yaml;
    private static final IODispatcher ioDispatcher = PunchTreeFreebuildPlugin.getIODispatcher();
    private boolean isDisconnected;
    private static final Logger LOGGER = Bukkit.getLogger();

    /**
     * Initializes the YamlDatabaseConnection with the specified file path.
     *
     * @param filePath The relative path of the YAML file to be used for storage.
     */
    public YamlDatabaseConnection(String filePath) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);

        this.yaml = new Yaml(options);

        this.filePath = Path.of(PunchTreeFreebuildPlugin.getInstance().getDataFolder().getPath(), filePath);
        this.isDisconnected = true;
    }

    /**
     * Connects to the YAML database, loading the data into memory.
     *
     * @param forced If true, the connection will create the file if it does not exist.
     * @return A CompletableFuture that resolves with the connected YamlDatabaseConnection.
     */
    @Override
    public CompletableFuture<YamlDatabaseConnection> connect(boolean forced) {
        enforceConnectionRequired(false);
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
     * Asynchronously saves the current data to the YAML file.
     *
     * @return A CompletableFuture that resolves with the YamlDatabaseConnection after saving.
     */
    public CompletableFuture<YamlDatabaseConnection> save() {
        Map<String, Object> dataCopy = new LinkedHashMap<>(data);
        return ioDispatcher.submitYamlTask(() -> {
            try (Writer writer = new FileWriter(filePath.toFile())) {
                yaml.dump(dataCopy, writer);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save YAML file", e);
            }
            return this;
        });
    }

    /**
     * Disconnects from the YAML database, saving data to the file and releasing resources.
     */
    @Override
    public void disconnect() {
        enforceConnectionRequired(true);
        data = null;
        isDisconnected = true;
    }

    /**
     * Creates a new entry in the specified table with the provided values.
     *
     * @param table  The table where the entry should be created.
     * @param values The values for the new entry.
     * @return The updated table.
     * @throws PathCreationFailedException If the specified path cannot be created.
     * @throws IllegalStateException If the operation is performed on a disconnected YamlDatabaseConnection.
     */
    @Override
    public Map<String, Object> create(String table, Map<String, Object> values) throws PathCreationFailedException, IllegalStateException {
        enforceConnectionRequired(true);
        Map<String, Object> locatedMap = MapUtils.getMapAtPath(data, table, true);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to create the path: " + table);
        }
        locatedMap.putAll(values);
        return locatedMap;
    }

    /**
     * Reads the values stored in the specified table.
     *
     * @param table The table to read from.
     * @return An Optional containing the table's values if it exists, or an empty Optional if not.
     * @throws IllegalStateException If the operation is performed on a disconnected YamlDatabaseConnection.
     */
    @Override
    public Optional<Map<String, Object>> read(String table) throws IllegalStateException {
        enforceConnectionRequired(true);
        return Optional.ofNullable(MapUtils.getMapAtPath(data, table, false));
    }

    /**
     * Updates an existing entry in the specified table with the provided values.
     *
     * @param table  The table where the entry should be updated.
     * @param values The values to update the entry with.
     * @return The updated table.
     * @throws PathCreationFailedException If the specified path cannot be found.
     * @throws IllegalStateException If the operation is performed on a disconnected YamlDatabaseConnection.
     */
    @Override
    public Map<String, Object> update(String table, Map<String, Object> values) throws PathCreationFailedException, IllegalStateException {
        enforceConnectionRequired(true);
        Map<String, Object> locatedMap = MapUtils.getMapAtPath(data, table, false);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to find the path: " + table);
        }
        locatedMap.putAll(values);
        return locatedMap;
    }

    /**
     * Deletes an existing entry in the specified table.
     *
     * @param table The table where the entry should be deleted.
     * @return The deleted entry.
     * @throws PathCreationFailedException If the specified path cannot be found.
     * @throws IllegalStateException If the operation is performed on a disconnected YamlDatabaseConnection.
     */
    @Override
    public Map<String, Object> delete(String table) throws PathCreationFailedException, IllegalStateException {
        enforceConnectionRequired(true);
        Map<String, Object> locatedMap = MapUtils.getMapAtPath(data, table, false);
        if (locatedMap == null) {
            throw new PathCreationFailedException("Failed to find the path: " + table);
        }
        Map<String, Object> clonedMap = new LinkedHashMap<>(locatedMap);
        locatedMap.clear();
        return clonedMap;
    }

/**
 * Updates or inserts an entry in the specified table with the provided values.
 *
 * @param table  The table where the entry should be upserted.
 * @param values The values to update or insert the entry with.
 * @return The updated table.
 * @throws PathCreationFailedException If the specified path cannot be created.
 * @throws IllegalStateException If the operation is performed on a disconnected YamlDatabaseConnection.
 */
@Override
public Map<String, Object> upsert(String table, Map<String, Object> values) throws PathCreationFailedException {
    enforceConnectionRequired(true);
    Map<String, Object> locatedMap = MapUtils.getMapAtPath(data, table, true);
    if (locatedMap == null) {
        throw new PathCreationFailedException("Failed to create the path: " + table);
    }
    locatedMap.putAll(values);
    return locatedMap;
}

    /**
     * Enforces connection requirements for CRUD operations.
     *
     * @param connectionRequired If true, checks if the connection is established; if false, checks if the connection is disconnected.
     * @throws IllegalStateException If the connection status does not match the required state.
     */
    private void enforceConnectionRequired(boolean connectionRequired) {
        if ((connectionRequired && isDisconnected) || (!connectionRequired && !isDisconnected)) {
            throw new IllegalStateException("Cannot perform operation on a " + (isDisconnected ? "disconnected" : "connected") + " YamlDatabaseConnection");
        }
    }

    /**
     * Ensures the file exists if the forced flag is true.
     *
     * @param forced If true, create the file and its parent directories if they do not exist.
     * @throws IOException If there's an error creating the file or directories.
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
     * Handles the loaded data, initializing the internal data map.
     *
     * @param forced If true, creates a new LinkedHashMap if the loaded data is null.
     * @param loadedData The data loaded from the YAML file.
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
     * A custom exception to be thrown when a path cannot be created or found.
     */
    private static class PathCreationFailedException extends Exception {
        public PathCreationFailedException(String message) {
            super("Failed to create/find the path: " + message);
        }
    }
}