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
package io.github.reflxction.warps.warp;

import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.config.PlayerStoringStrategy;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.json.PlayerData;
import org.bukkit.OfflinePlayer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

public class WarpController {

    public static void createWarp(PlayerWarp warp) {
        OfflinePlayer owner = warp.getOwner();
        WarpsX.getPlugin().getWarpsTree().lazyLoad(owner, PlayerData.class).getWarps().put(warp.getKey(), warp);
        WarpsX.getPlugin().getWarpKeys().set(warp.getKey(), ((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).to(owner));
    }

    public static void removeWarp(String key) {
        String owner = WarpsX.getPlugin().getWarpKeys().getString(key);
        OfflinePlayer player = ((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).from(owner);
        WarpsX.getPlugin().getWarpsTree().lazyLoad(player, PlayerData.class).getWarps().remove(key);
        WarpsX.getPlugin().getWarpKeys().remove(key);
    }

    public static PlayerWarp getWarp(String key) {
        try {
            String owner = WarpsX.getPlugin().getWarpKeys().getString(key);
            OfflinePlayer player = ((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).from(owner);
            return WarpsX.getPlugin().getWarpsTree().lazyLoad(player, PlayerData.class).getWarps().get(key);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static Map<String, PlayerWarp> getWarps(OfflinePlayer player) {
        return WarpsX.getPlugin().getWarpsTree().lazyLoad(player, PlayerData.class).getWarps();
    }

    public static Set<String> getWarpKeys(OfflinePlayer player) {
        return WarpsX.getPlugin().getWarpKeys().getContent().entrySet().stream()
                .filter(member -> member.getValue().getAsString().equals(((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).to(player)))
                .map(Entry::getKey).collect(Collectors.toSet());
    }
}