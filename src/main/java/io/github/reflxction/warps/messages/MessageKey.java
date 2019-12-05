/*
 * * Copyright 2019 github.com/ReflxctionDev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.reflxction.warps.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.warp.PlayerWarp;
import net.moltenjson.configuration.direct.DirectConfiguration;
import net.moltenjson.json.JsonFile;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class with all the message keys
 */
public enum MessageKey {

    /* Success messages */
    WARP_CREATED("warpCreated", MessageCategory.SUCCESS, "Warp created", "Sent when a warp has been created"),
    WARP_PUBLIC("warpPublic", MessageCategory.SUCCESS, "Warp set to public", "Sent when a warp becomes public"),
    WARP_PRIVATE("warpPrivate", MessageCategory.SUCCESS, "Warp set to private", "Sent when a warp becomes private"),
    INVITED_PLAYER("invitedPlayer", MessageCategory.SUCCESS, "Invited player", "Sent when a player invites another player to their warp"),
    UNINVITED_PLAYER("uninvitedPlayer", MessageCategory.SUCCESS, "Uninvited player", "Sent when a player uninvites another player from their warp"),
    UNINVITED_ALL("uninvitedAll", MessageCategory.SUCCESS, "Uninvited all", "Sent when a player uninvites all players from their warp"),
    WARP_SAFE("warpSafe", MessageCategory.SUCCESS, "Warp is safe", "Sent when a player checks the safety of a warp"),
    WARP_DELETED("warpDeleted", MessageCategory.SUCCESS, "Warp deleted", "Sent when a player deletes a warp"),
    GREETING_UPDATED("greetingMessageUpdated", MessageCategory.SUCCESS, "Greeting message updated", "Sent when a player updates the greeting message of a warp"),
    LOCATION_UPDATED("locationUpdated", MessageCategory.SUCCESS, "Location updated", "Sent when a player changes the location of a warp"),
    PUBLIC_NOTE("publicNote", MessageCategory.SUCCESS, "Warp public (note)", "Sent when a player uninvites all players from their warp when it is public"),
    SOUND_ADDED("soundAdded", MessageCategory.SUCCESS, "Sound added", "Sent when a player sets the cue/sound of their warp"),
    SOUND_REMOVED("soundRemoved", MessageCategory.SUCCESS, "Sound removed", "Sent when a player removes the cue/sound of their warp"),
    DELAY_SET("delaySet", MessageCategory.SUCCESS, "Delay set", "Sent when a player sets the delay of their warp"),
    PLAYER_BANNED("playerBanned", MessageCategory.SUCCESS, "Player banned", "Sent when a player bans another player from using their warp"),
    PLAYER_UNBANNED("playerUnbanned", MessageCategory.SUCCESS, "Player unbanned", "Sent when a player unbans another player from using their warp"),
    UNBANNED_ALL("unbannedAll", MessageCategory.SUCCESS, "Unbanned all", "Sent when a player unbans all players from their warp"),

    /* Error messages */
    NO_PERMISSION("noPermission", MessageCategory.ERROR, "No permission", "Sent when a player attempts to use a command but has no permission"),
    PLAYER_NOT_INVITED("playerNotInvited", MessageCategory.ERROR, "Player not invited", "Sent when a player attempts to uninvite a player but are not invited."),
    CREATE_BANNED("ownerBanned", MessageCategory.ERROR, "Player banned from creating warps", "Sent when a player attempts to create a warp but are banned"),
    USER_BANNED("userBanned", MessageCategory.ERROR, "Player banned from using warps", "Sent when a player attempts to use a warp but are banned"),
    MUST_WAIT("mustWait", MessageCategory.ERROR, "Wait for delay", "Sent when a player attempts to use a warp but must wait for the delay"),
    WARP_LIMIT("warpLimit", MessageCategory.ERROR, "Reached warp limit", "Sent when a player attempts to create a warp but have reached the limit"),
    NO_WARPS("noWarps", MessageCategory.ERROR, "Player has no warps", "Sent when a player attempts to list warps but does not have any"),
    WARP_BANNED("warpBanned", MessageCategory.ERROR, "Warp banned", "Sent when a player attempts to teleport to a banned warp"),
    PLAYER_ALREADY_BANNED("playerAlreadyBanned", MessageCategory.ERROR, "Player already banned", "Sent when a player attempts to ban a player that is already banned from using their warp"),
    PLAYER_NOT_BANNED("playerNotBanned", MessageCategory.ERROR, "Player not banned", "Sent when a player attempts to unban a player that is not banned from using their warp"),
    BANNED_FROM_WARP("bannedFromWarp", MessageCategory.ERROR, "Banned from warp", "Sent when a player attempts to use a warp but are banned from it"),
    WARP_NOT_SAFE("warpNotSafe", MessageCategory.ERROR, "Warp not safe", "Sent when a player attempts to use an unsafe warp."),

    CANNOT_INVITE_SELF("cannotInviteSelf", MessageCategory.ERROR, "Cannot invite self", "Sent when a player attempts to invite themselves to their own warp"),
    CANNOT_UNINVITE_SELF("cannotUninviteSelf", MessageCategory.ERROR, "Cannot uninvite self", "Sent when a player attempts to uninvite themselves from their own warp"),
    NOT_WARP_OWNER("notWarpOwner", MessageCategory.ERROR, "Not warp owner", "Sent when a player attempts to modify a warp but are not the owner of it"),
    NOT_INVITED("notInvited", MessageCategory.ERROR, "Not invited", "Sent when a player attempts to go to a warp but are not invited"),
    WARP_ALREADY_EXISTS("warpAlreadyExists", MessageCategory.ERROR, "Warp already exists", "Sent when a player attempts to create a warp but one with the same key exists");

    private String key;

    private MessageCategory category;

    private String message;

    private String name;

    private String description;

    MessageKey(String key, MessageCategory category, String name, String description) {
        this.key = key;
        this.category = category;
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the literal text of the message
     *
     * @return The message text
     */
    public String getText() {
        return message == null ? (message = MESSAGES_MAP.get(category).get(key)) : message;
    }

    /**
     * Requests the message and updates it
     */
    private void request() {
        message = MESSAGES_MAP.get(category).get(key);
    }

    /**
     * Returns the display name of the key
     *
     * @return The display name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the message
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sends the message by applying all placeholders. Null values will not get replaced
     *
     * @param sender   The entity to receive the message
     * @param location The location. Can be null
     * @param player   The player. Can be null
     */
    public void send(CommandSender sender, Location location, PlayerWarp warp, OfflinePlayer player, String... replacements) {
        String message = getText();
        if (message.equals("{}")) return;
        if (replacements.length != 0 && replacements.length % 2 == 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                String key = replacements[i];
                String value = replacements[i + 1];
                if (value == null) value = "";
                message = message.replace(key, value);
            }
        }
        if (warp != null) {
            message = message.replace("{warp_key}", warp.getKey())
                    .replace("{warp_greeting}", warp.getGreetingMessage())
                    .replace("{warp_delay}", Integer.toString(warp.getDelay()))
                    .replace("{warp_delay_plural}", warp.getDelay() == 1 ? "" : "s");
        }
        if (location != null) {
            message = message
                    .replace("{x}", Double.toString(location.getBlockX()))
                    .replace("{y}", Double.toString(location.getBlockY()))
                    .replace("{z}", Double.toString(location.getBlockZ()))
                    .replace("{world}", location.getWorld().getName());
        }
        if (player != null) {
            message = message.replace("{player}", player.getName());
            if (warp != null && player.isOnline()) {
                int delay = WarpsX.getPlugin().getDelayExecutor().getTimeLeft(player.getPlayer(), "warp=" + warp);
                message = message
                        .replace("{player_delay_plural}", delay == 1 ? "" : "s")
                        .replace("{player_delay}", Integer.toString(delay));
            }
        }
        Chat.plugin(sender, message);
    }

    /**
     * Sets the key's text
     *
     * @param text New text to set
     */
    public void setText(String text) {
        MESSAGES_MAP.get(category).put(key, text);
        message = text;
    }

    /**
     * The messages.json file
     */
    private static final DirectConfiguration MESSAGES_CONFIG = DirectConfiguration.of(JsonFile.of(WarpsX.getPlugin().getDataFolder(), "messages.json"));

    /**
     * A map that contains all messages mapped to their keys
     */
    private static final Map<MessageCategory, Map<String, String>> MESSAGES_MAP = new HashMap<>();

    /**
     * The reflective type of maps
     */
    private static final Type MAP_TYPE = new TypeToken<HashMap<String, String>>() {
    }.getType();

    /**
     * Gson used for reading and writing
     */
    private static final Gson GSON = new GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    /**
     * Maps all categories by their messages
     */
    private static final Map<MessageCategory, List<MessageKey>> BY_CATEGORY = new HashMap<>();

    /**
     * The chat prefix
     */
    private static String PREFIX;

    /**
     * Saves the messages to the config
     */
    public static void save() {
        MESSAGES_MAP.forEach((category, messages) -> MESSAGES_CONFIG.set(category.name().toLowerCase(), messages, GSON));
        MESSAGES_CONFIG.save(GSON, Throwable::printStackTrace);
    }

    /**
     * Returns the chat prefix
     *
     * @return The prefix
     */
    public static String prefix() {
        return PREFIX;
    }

    public static void load() {
        if (PREFIX != null) // it was already loaded before
            MESSAGES_CONFIG.refresh();
        PREFIX = Chat.colorize(MESSAGES_CONFIG.getString("globalPrefix"));
        for (MessageCategory category : MessageCategory.values()) {
            Map<String, String> map = MESSAGES_CONFIG.get(category.name().toLowerCase(), MAP_TYPE);
            map.replaceAll((key, value) -> Chat.colorize(value));
            MESSAGES_MAP.put(category, map);
        }
        for (MessageCategory category : MessageCategory.VALUES) {
            BY_CATEGORY.put(category, new LinkedList<>());
        }
        for (MessageKey key : values()) {
            BY_CATEGORY.get(key.category).add(key);
            key.request();
        }
    }

    /**
     * Returns all the message keys that are in a category
     *
     * @param category Category to get from
     * @return A list of all keys in that category
     */
    public static List<MessageKey> byCategory(MessageCategory category) {
        return BY_CATEGORY.get(category);
    }
}