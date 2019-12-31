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
package io.github.reflxction.warps.safety;

import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.api.WarpUseEvent;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.util.game.delay.DelayData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class WarpInvincibility implements Listener {

    private WarpsX plugin;

    public WarpInvincibility(WarpsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onWarpUse(WarpUseEvent event) {
        if (!((boolean) PluginSettings.SAFETY_INVINCIBILITY.get())) return;
        int time = PluginSettings.SAFETY_INVINCIBILITY_TIME.get();
        if (time > 0) {
            event.getPlayer().setInvulnerable(true);
            plugin.getDelayExecutor().setDelay(event.getPlayer(), "warp-safety", new DelayData(time).setOnFinish(player -> {
                if (player.isOnline()) player.getPlayer().setInvulnerable(false);
            }));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        if (plugin.getDelayExecutor().hasDelay((Player) event.getDamager(), "warp-safety"))
            event.setCancelled(true);
    }
}
