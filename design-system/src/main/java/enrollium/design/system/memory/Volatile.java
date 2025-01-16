package enrollium.design.system.memory;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;


/**
 * SingletonConcurrentMap is a thread-safe singleton class that provides a globally accessible
 * ConcurrentHashMap for storing and accessing data across the JavaFX project.
 *
 * Example:
 * ```
 * SingletonConcurrentMap globalMap = SingletonConcurrentMap.getInstance();
 * globalMap.put("username", "john_doe");
 * String username = (String) globalMap.get("username");
 * ```
 */
@Slf4j
public class Volatile {
    private static volatile Volatile                          instance = null;
    private final           ConcurrentHashMap<String, Object> dataMap  = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    private Volatile() {}

    /**
     * Provides the global access point to the SingletonConcurrentMap instance.
     * Implements double-checked locking for thread safety and lazy initialization.
     *
     * @return SingletonConcurrentMap instance
     */
    public static Volatile getInstance() {
        if (instance == null) synchronized (Volatile.class) {
            if (instance == null) instance = new Volatile();
        }

        return instance;
    }

    /**
     * Puts a key-value pair into the map.
     *
     * @param key   the key associated with the value
     * @param value the value to store
     */
    public void put(String key, Object value) {
        log.info("[Volatile] put => {} | {}", key, value);
        dataMap.put(key, value);
    }

    /**
     * Retrieves the value associated with the given key.
     *
     * @param key the key to search for
     *
     * @return the value associated with the key, or null if not found
     */
    public Object get(String key) {
        return dataMap.get(key);
    }

    /**
     * Removes the key-value pair associated with the given key.
     *
     * @param key the key to remove
     */
    public void remove(String key) {
        log.info("[Volatile] remove => {}", key);
        dataMap.remove(key);
    }

    /**
     * Checks if the map contains the specified key.
     *
     * @param key the key to check
     *
     * @return true if the key exists, false otherwise
     */
    public boolean containsKey(String key) {
        return dataMap.containsKey(key);
    }

    /**
     * Clears all entries in the map.
     */
    public void clear() {
        dataMap.clear();
    }
}
