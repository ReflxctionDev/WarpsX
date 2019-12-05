package io.github.reflxction.warps.util.game.delay;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * A simple delay management utility
 */
public class DelayExecutor {

    /**
     * The global delay map
     */
    private final Map<UUID, Map<String, DelayData>> delayMap = new HashMap<>();

    /**
     * The plugin instance
     */
    private Plugin plugin;

    /**
     * The delay task
     */
    private BukkitTask delayTask;

    /**
     * Creates a new delay executor for the specified plugin
     *
     * @param plugin Plugin to create for
     */
    public DelayExecutor(Plugin plugin) {
        Preconditions.checkNotNull(plugin, "Plugin must not be null!");
        this.plugin = plugin;
    }

    /**
     * Adds delay for the specified player
     *
     * @param player  Player to add delay for
     * @param context Context for the delay, e.g "command".
     * @param data    Delay data
     */
    public void setDelay(OfflinePlayer player, String context, DelayData data) {
        getDelayMap(player.getUniqueId()).put(context, data);
    }

    /**
     * Forcibly cancels the delay of the specified player in the context
     *
     * @param player  Player to cancel for
     * @param context Context to remove
     */
    public void cancelDelay(OfflinePlayer player, String context) {
        getDelayMap(player.getUniqueId()).remove(context);
    }

    /**
     * Returns the time left for the player in the specified context, or {@code 0} if
     * the player has no delay in that context.
     *
     * @param player  Player to get for
     * @param context The context
     * @return The time left, or {@code 0} if the player has no delay.
     */
    public int getTimeLeft(OfflinePlayer player, String context) {
        DelayData delayData = getDelayMap(player.getUniqueId()).get(context);
        return delayData == null ? 0 : delayData.getTimeLeft();
    }

    /**
     * Returns whether does the player have delay in the specified context
     *
     * @param player  Player to check for
     * @param context Context to check for
     * @return {@code true} if the player does have context, {@code false} if otherwise.
     */
    public boolean hasDelay(OfflinePlayer player, String context) {
        return getDelayMap(player.getUniqueId()).containsKey(context);
    }

    /**
     * Returns the {@link DelayData} of the specified context from the specified player.
     *
     * @param player  Player to retrieve for
     * @param context The context
     * @return The delay data, or {@code null} if not found.
     */
    public DelayData getDelayData(OfflinePlayer player, String context) {
        return getDelayMap(player.getUniqueId()).get(context);
    }

    /**
     * Cancels all the players' delays
     */
    public void cancelAllDelays() {
        delayMap.clear();
    }

    /**
     * Cancels all delays for the specified player
     *
     * @param player Player to clear for
     */
    public void cancelAllDelays(OfflinePlayer player) {
        getDelayMap(player.getUniqueId()).clear();
    }

    /**
     * Cancels the BukkitTask responsible for reducing delays
     */
    public void cancelBukkitTask() {
        delayTask.cancel();
    }

    /**
     * Starts the reducing task
     */
    public void start() {
        delayTask = Bukkit.getScheduler().runTaskTimer(plugin, this::reduceAll, 20, 20);
    }

    /**
     * Returns the plugin of this delay executor
     *
     * @return The plugin
     */
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * Reduces all delay by 1
     */
    private void reduceAll() {
        delayMap.forEach((uuid, map) -> {
            for (Iterator<Entry<String, DelayData>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
                Entry<String, DelayData> entry = iterator.next();
                DelayData delayData = entry.getValue();
                if (delayData.reduce() <= 0) {
                    iterator.remove();
                    delayData.finish(uuid);
                }
            }
        });
    }

    /**
     * Returns the delay map for the specified player
     *
     * @param player Player to retrieve for
     * @return The player map
     */
    private Map<String, DelayData> getDelayMap(UUID player) {
        return delayMap.computeIfAbsent(player, (uuid) -> new HashMap<>());
    }
}