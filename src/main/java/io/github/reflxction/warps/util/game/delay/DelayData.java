package io.github.reflxction.warps.util.game.delay;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Represents data for delays
 */
public class DelayData {

    /**
     * The time left for the delay
     */
    private int timeLeft;

    /**
     * Task executed when the delay is finished
     */
    private Consumer<OfflinePlayer> onFinish;

    /**
     * Other data stored by the delay
     */
    private Map<Object, Object> data = new HashMap<>();

    /**
     * Creates a new delay data
     *
     * @param timeLeft Time left for the delay
     */
    public DelayData(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    /**
     * Returns the time left for the delay
     *
     * @return The time left
     */
    public int getTimeLeft() {
        return timeLeft;
    }

    /**
     * Reduces the delay
     *
     * @return The time before getting reduced
     */
    protected int reduce() {
        return --timeLeft;
    }

    /**
     * Puts the specified data in the delay data map
     *
     * @param key   Key of the data
     * @param value Value of the data
     * @return This object instance
     */
    public DelayData data(Object key, Object value) {
        data.put(key, value);
        return this;
    }

    /**
     * Returns the specified object from the key
     *
     * @param key Key to retrieve from
     * @param <T> The object type
     * @return The object, or {@code null} if not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Object key) {
        return (T) data.get(key);
    }

    /**
     * Sets the task of finishing
     *
     * @param onFinish Task to run when the delay is over. Can be null.
     * @return This object instance.
     */
    public DelayData setOnFinish(Consumer<OfflinePlayer> onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    /**
     * Invokes the finishing callback on the specified UUID
     *
     * @param uuid UUID to run on
     */
    public void finish(UUID uuid) {
        if (onFinish != null)
            onFinish.accept(Bukkit.getOfflinePlayer(uuid));
    }
}