package io.github.reflxction.warps.config;

import io.github.reflxction.warps.WarpsX;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Contains all plugin settings
 */
@SuppressWarnings("unchecked") // Lots of casts for generics
public enum PluginSettings {

    STORING_STRATEGY("Storage.PlayerStoringStrategy", PlayerStoringStrategy.UUID),
    DEFAULT_GREETING_MESSAGE("Warps.DefaultGreetingMessage", "&eWelcome to warp &d{warp}&e!"),
    DEFAULT_DELAY("Warps.DefaultDelay", 10),
    SAFETY_RADIUS("WarpSafety.SafetyCuboidLength", 5),
    SAFETY_WARN("WarpSafety.WarnBeforeWarping", true),
    SAFETY_CHECKS("WarpSafety.Checks", Collections.emptyList());

    public static final PluginSettings[] values = values();

    /**
     * The config path to the variable
     */
    private String path;

    /**
     * The default value
     */
    private Object defaultValue;

    /**
     * Represents the value
     */
    private Object value;

    /**
     * Creates a new setting
     *
     * @param path         Path to the field
     * @param defaultValue Default value
     */
    PluginSettings(String path, Object defaultValue) {
        this.path = path;
        this.defaultValue = defaultValue;
        value = request();
    }

    /**
     * Returns the field from the cache
     *
     * @param <R> Field type
     * @return The field value
     */
    public <R> R get() {
        return value == null ? request() : (R) value;
    }

    /**
     * Returns the field directly from config
     *
     * @param <R> Field type
     * @return The field value
     */
    public <R> R request() {
        if (defaultValue instanceof Map)
            return (R) (value = requestMap());
        if (defaultValue instanceof Enum)
            return (R) (value = requestEnum());
        return (R) (value = WarpsX.getPlugin().getConfig().get(path, defaultValue));
    }

    /**
     * Returns a {@link Map} derived from a section
     *
     * @param <V> Value type
     * @return The map
     */
    private <V> Map<String, V> requestMap() {
        return getMap(WarpsX.getPlugin().getConfig(), path);
    }

    /**
     * Returns an enumeration of this value
     *
     * @param <E> Enum type
     * @return The enumeration value
     */
    private <E extends Enum> E requestEnum() {
        try {
            return (E) Enum.valueOf(((Enum) defaultValue).getDeclaringClass(), WarpsX.getPlugin().getConfig().getString(path, ((Enum) defaultValue).name()).toUpperCase());
        } catch (IllegalArgumentException e) {
            return (E) defaultValue;
        }
    }

    /**
     * Returns an enumeration of this value
     *
     * @param <E> Enum type
     * @return The enumeration value
     */
    @SuppressWarnings("RedundantCast") // it does not compile if I remove the cast, so tehcnically its not redundant but intellij is stupid
    public <E extends Enum> Set<E> requestEnumSet(Class<E> clazz) {
        List<String> values = WarpsX.getPlugin().getConfig().getStringList(path);
        return ((Stream<E>) values.stream().map(s -> (E) Enum.valueOf(clazz, s.toUpperCase()))).collect(Collectors.toSet());
    }

    /**
     * Returns a map which contains all elements inside it in order. Sub-maps are added
     * recursively.
     *
     * @param c    Configuration to get from
     * @param path Path of the map
     * @param <V>  Map value type
     * @return The map
     */
    private static <V> Map<String, V> getMap(FileConfiguration c, String path) {
        Map<String, V> map = new LinkedHashMap<>();
        for (String k : c.getConfigurationSection(path).getKeys(false)) {
            Object o = c.get(path + "." + k);
            if (o instanceof MemorySection) {
                map.put(k, (V) getMap(c, path + "." + k));
                continue;
            }
            map.put(k, (V) o);
        }
        return map;
    }

    /**
     * Invoked in order to load the class
     */
    public static void load() {
    }

}