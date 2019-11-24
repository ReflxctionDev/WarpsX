package io.github.reflxction.warps;

import co.aikar.commands.*;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.moltenjson.configuration.direct.DirectConfiguration;
import io.github.moltenjson.configuration.select.SelectableConfiguration;
import io.github.moltenjson.configuration.tree.TreeConfiguration;
import io.github.moltenjson.configuration.tree.TreeConfigurationBuilder;
import io.github.moltenjson.json.JsonFile;
import io.github.reflxction.warps.command.*;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.gui.WarpGUI;
import io.github.reflxction.warps.json.NamingStrategy;
import io.github.reflxction.warps.json.PlayerData;
import io.github.reflxction.warps.json.PluginData;
import io.github.reflxction.warps.json.adapter.EnchantmentsAdapter;
import io.github.reflxction.warps.json.adapter.LocationAdapter;
import io.github.reflxction.warps.json.adapter.OfflinePlayerAdapter;
import io.github.reflxction.warps.json.adapter.PotionEffectsAdapter;
import io.github.reflxction.warps.listener.JoinListener;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.messages.MessageKey;
import io.github.reflxction.warps.util.FileManager;
import io.github.reflxction.warps.util.compatibility.Compatibility;
import io.github.reflxction.warps.util.game.DelayManager;
import io.github.reflxction.warps.warp.PlayerWarp;
import io.github.reflxction.warps.warp.WarpController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static co.aikar.commands.ACFBukkitUtil.color;

/**
 * WarpsX: A powerful per-user warps plugin
 */
public final class WarpsX extends JavaPlugin {

    /**
     * Gson used for JSON conversions
     */
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(OfflinePlayer.class, OfflinePlayerAdapter.INSTANCE)
            .registerTypeAdapter(Player.class, OfflinePlayerAdapter.INSTANCE)
            .registerTypeAdapter(EnchantmentsAdapter.TYPE, EnchantmentsAdapter.INSTANCE)
            .registerTypeAdapter(Location.class, new LocationAdapter())
            .registerTypeAdapter(PotionEffectsAdapter.TYPE, PotionEffectsAdapter.INSTANCE)
            .registerTypeAdapter(PlayerWarp.class, new PlayerWarp.Creator())
            .disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .setPrettyPrinting().create();

    /**
     * Plugin instance
     */
    private static WarpsX plugin;

    /**
     * File manager
     */
    private FileManager<WarpsX> fileManager = new FileManager<>(this);

    private SelectableConfiguration pluginData = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("plugin-data.json")), false, GSON)
            .register(PluginData.class);

    private SelectableConfiguration warpsGUI = SelectableConfiguration.of(JsonFile.of(fileManager.createFile("warps-gui.json")), false, GSON)
            .register(WarpGUI.class);

    /**
     * The data tree
     */
    private TreeConfiguration<OfflinePlayer, PlayerData> warpsTree = new TreeConfigurationBuilder<OfflinePlayer, PlayerData>
            (fileManager.createDirectory("data"), NamingStrategy.INSTANCE)
            .setLazy(true)
            .setGson(GSON)
            .setRestrictedExtensions(ImmutableList.of("json"))
            .build();

    /**
     * The storage for warps
     */
    private DirectConfiguration warpKeys = DirectConfiguration.of(JsonFile.of(fileManager.createFile("warp-keys.json")));

    /**
     * Plugin startup logic
     */
    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        PluginSettings.load();
        MessageKey.load();
        PaperCommandManager commandManager = new PaperCommandManager(this);
        initCommandManager(commandManager);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(new WarpGUI(), this);
        getServer().getPluginManager().registerEvents(WarpsXCommand.INSTANCE, this);
        DelayManager.start(this);
        pluginData.associate();
        warpsGUI.associate();
    }

    public TreeConfiguration<OfflinePlayer, PlayerData> getWarpsTree() {
        return warpsTree;
    }

    public DirectConfiguration getWarpKeys() {
        return warpKeys;
    }

    /**
     * Plugin shutdown logic
     */
    @Override
    public void onDisable() {
        MessageKey.save();
        warpKeys.save(Throwable::printStackTrace, GSON);
        try {
            warpsTree.lazySave();
        } catch (IOException e) {
            getLogger().severe("Unable to save data");
            e.printStackTrace();
        }
        pluginData.save();
    }

    /**
     * Returns the plugin instance
     *
     * @return The plugin instance
     */
    public static WarpsX getPlugin() {
        return plugin;
    }

    private void initCommandManager(PaperCommandManager commandManager) {
        //<editor-fold desc="contexts and commands" defaultstate="collapsed">
        commandManager.getCommandContexts().registerContext(PlayerWarp.class, (c) -> {
            String key = c.popFirstArg();
            PlayerWarp warp = WarpController.getWarp(key);
            if (warp == null)
                throw new InvalidCommandArgument(color("&cInvalid warp: &e" + key), false);
            return warp;
        });
        commandManager.getCommandContexts().registerContext(boolean.class, WarpsX::getBoolean);
        commandManager.getCommandContexts().registerContext(OfflinePlayer.class, c -> {
            String name = c.popFirstArg();
            UUID uuid = null;
            if (c.hasFlag("uuid")) {
                uuid = UUID.fromString(name);
            }
            OfflinePlayer offlinePlayer = uuid != null ? Bukkit.getOfflinePlayer(uuid) : Bukkit.getOfflinePlayer(name);
            if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
                throw new InvalidCommandArgument(MinecraftMessageKeys.NO_PLAYER_FOUND_OFFLINE,
                        false, "{search}", name);
            }
            return offlinePlayer;
        });

        commandManager.getCommandContexts().registerContext(Sound.class, (c) -> {
            String s = c.popFirstArg();
            Sound sound = Compatibility.getSound(s);
            if (sound == null)
                throw new InvalidCommandArgument(color("&cInvalid sound: &e" + s), false);
            return sound;
        });
        commandManager.enableUnstableAPI("help");
        commandManager.getCommandConditions().addCondition("player", (c) -> {
            if (!c.getIssuer().isPlayer())
                throw new ConditionFailedException(Chat.colorize("&cYou must be a player to use this command!"));
        });
        commandManager.getCommandReplacements().addReplacement("admin", "warpsx.admin");
        commandManager.getCommandCompletions().registerStaticCompletion("booleans", Arrays.asList("true", "false", "toggle"));
        commandManager.getCommandCompletions().registerStaticCompletion("confirmation", Collections.singletonList("confirm"));
        commandManager.getCommandCompletions().registerStaticCompletion("reloadable", Arrays.asList("config", "messages", "warps-gui"));
        commandManager.getCommandCompletions().registerCompletion("playerwarps", (c) -> warpsTree.lazyLoad(c.getPlayer(), PlayerData.class).getWarps().keySet());
        commandManager.getCommandCompletions().registerCompletion("sounds", (c) -> Arrays.stream(Sound.values()).map(s -> s.name().toLowerCase()).collect(Collectors.toList()));
        commandManager.getCommandCompletions().registerCompletion("effecttypes", (c) -> Arrays.stream(PotionEffectType.values()).map(s -> s.getName().toLowerCase()).collect(Collectors.toList()));
        commandManager.registerCommand(new WarpsCommand(this));
        commandManager.registerCommand(new AdminWarpsCommand());
        commandManager.registerCommand(new ToWarpCommand());
        commandManager.registerCommand(new WarpsGUICommand());
        commandManager.registerCommand(new WarpSafetyCommand());
        commandManager.registerCommand(WarpsXCommand.INSTANCE);
        //</editor-fold>
    }

    {
        fileManager.createFile("messages.json");
    }

    public SelectableConfiguration getWarpsGUI() {
        return warpsGUI;
    }

    private static boolean getBoolean(BukkitCommandExecutionContext c) {
        String v = c.popFirstArg();
        switch (v) {
            case "t":
            case "true":
            case "on":
            case "y":
            case "yes":
            case "1":
            case "confirm":
            case "definitely":
                return true;
            default:
                return false;
        }
    }
}