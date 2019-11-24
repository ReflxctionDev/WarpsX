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
package io.github.reflxction.warps.util.game;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class DelayManager {

    private static final Multimap<UUID, WarpDelay> DELAY_MAP = ArrayListMultimap.create();

    private static final Map<PlayerWarp, Integer> OPEN = new HashMap<>();

    public static void delayPlayer(Player player, PlayerWarp warp) {
        DELAY_MAP.put(player.getUniqueId(), new WarpDelay(warp));
    }

    public static void startExclusion(PlayerWarp warp) {
        OPEN.put(warp, warp.getExclusion());
        warp.setPublic(true);
    }

    public static int getDelay(Player player, PlayerWarp warp) {
        if (player.hasPermission("warpsx.admin.bypass.delay")) return 0;
        WarpDelay delay = DELAY_MAP.get(player.getUniqueId()).stream().filter(d -> d.warp.getKey().equals(warp.getKey())).findFirst()
                .orElse(null);
        if (delay == null) return 0;
        return delay.getTimeLeft();
    }

    private static void reduceAll() {
        DELAY_MAP.values().removeIf(delay -> delay.reduceTime() <= 0);

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
        Bukkit.getScheduler().runTaskTimer(plugin, DelayManager::reduceAll, 20, 20);
    }

    private static class WarpDelay {

        private PlayerWarp warp;
        private int timeLeft;

        WarpDelay(PlayerWarp warp) {
            this.warp = warp;
            this.timeLeft = warp.getDelay();
        }

        public PlayerWarp getWarp() {
            return warp;
        }

        int getTimeLeft() {
            return timeLeft;
        }

        int reduceTime() {
            return timeLeft--;
        }

    }

}
