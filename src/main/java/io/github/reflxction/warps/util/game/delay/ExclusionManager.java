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
package io.github.reflxction.warps.util.game.delay;

import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A utility for managing exclusions
 */
public class ExclusionManager {

    private static final Map<PlayerWarp, Integer> OPEN = new HashMap<>();

    public static void startExclusion(PlayerWarp warp) {
        OPEN.put(warp, warp.getExclusion());
        warp.setPublic(true);
    }

    private static void reduceAll() {
        for (Iterator<Entry<PlayerWarp, Integer>> iterator = OPEN.entrySet().iterator(); iterator.hasNext(); ) {
            Entry<PlayerWarp, Integer> entry = iterator.next();
            PlayerWarp warp = entry.getKey();
            int ex = entry.getValue();
            ex--;
            entry.setValue(ex);
            if (ex <= 0) {
                iterator.remove();
                warp.setPublic(false);
                if (warp.getOwner().getPlayer() != null)
                    Chat.admin(warp.getOwner().getPlayer(), "&aYour warp is no longer controlled by the exclusion");
            }
        }
    }

    public static void start(WarpsX plugin) {
        Bukkit.getScheduler().runTaskTimer(plugin, ExclusionManager::reduceAll, 20, 20);
    }

}
