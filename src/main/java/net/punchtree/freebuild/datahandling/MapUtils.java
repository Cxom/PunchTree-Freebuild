package net.punchtree.freebuild.datahandling;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtils {
    /**
     * Retrieves the map located at the specified dot-separated path in the given map.
     * If the forcePath parameter is true, creates the necessary structure along the path
     * if it does not exist.
     *
     * @param map      The map to traverse.
     * @param path     The dot-separated path to the desired map (e.g., "parent.child").
     * @param forcePath If true, creates the necessary structure along the path if it does not exist.
     * @return The map located at the specified path or null if the path does not exist and forcePath is false.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMapAtPath(Map<String, Object> map, String path, boolean forcePath) {
        if (path.isEmpty()) {
            return map;
        }
        String[] keys = path.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];

            if (i == keys.length - 1) {
                if (forcePath && !currentMap.containsKey(key)) {
                    currentMap.put(key, new LinkedHashMap<String, Object>());
                }
                Object value = currentMap.get(key);
                if (value instanceof Map) {
                    return (Map<String, Object>) value;
                } else {
                    return null;
                }
            }

            currentMap = getNextMap(currentMap, key, forcePath);

            if (currentMap == null) {
                return null;
            }
        }
        return currentMap;
    }

    /**
     * Retrieves the value at the specified path in the given map.
     *
     * @param currentMap The map to traverse.
     * @param table      The dot-separated path to the desired table (e.g., "grandparent.parent").
     * @param key        The single key to the desired value. (e.g., "child").
     * @return The value located at the specified path or null if the path does not exist.
     */
    public static Object getNestedValue(Map<String, Object> currentMap, String table, String key) {
        Map<String, Object> tableMap = getMapAtPath(currentMap, table, false);
        if (tableMap == null) {
            return null;
        }
        return tableMap.get(key);
    }

    /**
     * Sets the value at the specified path in the given map.
     *
     * @param currentMap The map to traverse.
     * @param table      The dot-separated path to the desired table (e.g., "grandparent.parent").
     * @param key        The single key to the desired value. (e.g., "child").
     * @param value      The value to set.
     */
    public static void setNestedValue(Map<String, Object> currentMap, String table, String key, Object value) {
        Map<String, Object> tableMap = getMapAtPath(currentMap, table, true);
        if (tableMap == null) {
            return; // TODO: throw exception (should never happen)
        }
        tableMap.put(key, value);
    }

    /**
     * Retrieves or creates the next map in the path based on the current map,
     * key, and forcePath flag.
     *
     * @param currentMap The current map being traversed.
     * @param key        The key for the next map.
     * @param forcePath  If * true, creates a new map for the key if it does not exist.
     * @return The next map in the path or null if the key does not exist and forcePath is false.
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getNextMap(Map<String, Object> currentMap, String key, boolean forcePath) {
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
    private static Map<String, Object> createNewMapAndPutInCurrentMap(Map<String, Object> currentMap, String key) {
        Map<String, Object> newMap = new LinkedHashMap<>();
        currentMap.put(key, newMap);
        return newMap;
    }
}