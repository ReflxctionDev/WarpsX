package io.github.reflxction.warps;

import co.aikar.commands.*;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.reflxction.warps.command.*;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.gui.WarpGUI;
import io.github.reflxction.warps.hook.GPHook;
import io.github.reflxction.warps.hook.HookRegistry;
import io.github.reflxction.warps.hook.VaultHook;
import io.github.reflxction.warps.json.NamingStrategy;
import io.github.reflxction.warps.json.PlayerData;
import io.github.reflxction.warps.json.PluginData;
import io.github.reflxction.warps.json.adapter.EnchantmentsAdapter;
import io.github.reflxction.warps.json.adapter.LocationAdapter;
import io.github.reflxction.warps.json.adapter.OfflinePlayerAdapter;
import io.github.reflxction.warps.json.adapter.PotionEffectsAdapter;
import io.github.reflxction.warps.listener.CommandListener;
import io.github.reflxction.warps.listener.JoinListener;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.messages.MessageKey;
import io.github.reflxction.warps.safety.WarpInvincibility;
import io.github.reflxction.warps.util.FileManager;
import io.github.reflxction.warps.util.compatibility.Compatibility;
import io.github.reflxction.warps.util.game.delay.DelayExecutor;
import io.github.reflxction.warps.util.game.delay.ExclusionManager;
import io.github.reflxction.warps.warp.PlayerWarp;
import io.github.reflxction.warps.warp.WarpController;
import net.moltenjson.configuration.direct.DirectConfiguration;
import net.moltenjson.configuration.pack.ConfigurationPack;
import net.moltenjson.configuration.pack.DeriveFrom;
import net.moltenjson.configuration.tree.TreeConfiguration;
import net.moltenjson.configuration.tree.TreeConfigurationBuilder;
import net.moltenjson.json.JsonFile;
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

    private DelayExecutor delayExecutor = new DelayExecutor(this);

    /**
     * File manager
     */
    private FileManager<WarpsX> fileManager = new FileManager<>(this);

    private ConfigurationPack<WarpsX> configurationPack = new ConfigurationPack<>(this, getDataFolder(), GSON);

    @DeriveFrom("plugin-data.json")
    private static PluginData pluginData = new PluginData();

    @DeriveFrom("warps-gui.json")
    private static WarpGUI warpGUI = new WarpGUI();

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
        getServer().getPluginManager().registerEvents(new CommandListener(), this);
        getServer().getPluginManager().registerEvents(warpGUI, this);
        getServer().getPluginManager().registerEvents(new WarpInvincibility(this), this);
        getServer().getPluginManager().registerEvents(WarpsXCommand.INSTANCE, this);
        ExclusionManager.start(this);
        delayExecutor.start();
        try {
            configurationPack.register();
        } catch (IOException e) {
            e.printStackTrace();
        }
        HookRegistry.registerAllHooks();
        if (HookRegistry.isHookEnabled(VaultHook.class))
            getServer().getPluginManager().registerEvents(new VaultHook(), this);
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
        warpKeys.save(GSON, Throwable::printStackTrace);
        try {
            warpsTree.lazySave();
            configurationPack.saveField("pluginData");
        } catch (IOException e) {
            getLogger().severe("Unable to save data");
            e.printStackTrace();
        }
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
        commandManager.getCommandConditions().addCondition("claim", (c) -> {
            if (!((boolean) PluginSettings.GRIEFPREVENTION_CHECK_CLAIM.get())) return;
            Player player = c.getIssuer().getPlayer();
            if (HookRegistry.isHookEnabled(GPHook.class) && !GPHook.isOwnerAtLocation(player, player.getLocation()))
                throw new ConditionFailedException(Chat.colorize("&cYou cannot set warps in the claims of other players!"));
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

    public static WarpGUI getWarpGUI() {
        return warpGUI;
    }

    public static PluginData getPluginData() {
        return pluginData;
    }

    public DelayExecutor getDelayExecutor() {
        return delayExecutor;
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

    {
        fileManager.createFile("messages.json");
        fileManager.createFile("plugin-data.json");
        fileManager.createFile("warps-gui.json");
    }

    public ConfigurationPack<WarpsX> getConfigurationPack() {
        return configurationPack;
    }
}