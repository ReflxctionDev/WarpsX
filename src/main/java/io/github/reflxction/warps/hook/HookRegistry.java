package io.github.reflxction.warps.hook;

import io.github.reflxction.warps.WarpsX;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The hook registr
 */
public class HookRegistry {

    /**
     * The hooks package
     */
    private static final String PACKAGE = "io.github.reflxction.warps.hook";

    /**
     * The hook plugins
     */
    private static final Map<String, Plugin> HOOK_PLUGINS = new HashMap<>();

    /**
     * Enabled hooks
     */
    private static final List<Class<?>> ENABLED_HOOKS = new ArrayList<>();

    /**
     * Registers all plugin hooks in this package
     */
    public static void registerAllHooks() {
        Reflections.log = null; // We don't want these log messages. (Absolutely safe, and the JAR is relocated anyway so it will not affect other plugins.)
        Reflections reflections = new Reflections(PACKAGE);
        for (Class<?> hookClass : reflections.getTypesAnnotatedWith(PluginHook.class)) {
            PluginHook hook = hookClass.getAnnotation(PluginHook.class);
            Plugin plugin = Bukkit.getPluginManager().getPlugin(hook.requiredPlugin());
            if (plugin == null) {
                WarpsX.getPlugin().getLogger().info(hook.requiredPlugin() + " not found. Hook disabled.");
                return;
            }
            ENABLED_HOOKS.add(hookClass);
            HOOK_PLUGINS.put(hook.requiredPlugin(), plugin);
            WarpsX.getPlugin().getLogger().info(plugin.getName() + " found. Enabling hook.");
        }
    }

    public static boolean isHookEnabled(Class<?> hookClass) {
        return ENABLED_HOOKS.contains(hookClass);
    }

    /**
     * Returns the specified plugin
     *
     * @param name Plugin name
     * @param <R>  The plugin type
     * @return The plugin
     */
    @SuppressWarnings("unchecked")
    public static <R extends Plugin> R getPlugin(String name) {
        return (R) HOOK_PLUGINS.get(name);
    }

}