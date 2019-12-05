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
package io.github.reflxction.warps.listener;

import io.github.reflxction.warps.api.WarpUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class CommandListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onWarpUse(WarpUseEvent event) {
        event.getWarp().getCommands().stream().map(c -> c.replace("{player}", event.getPlayer().getName()))
                .forEach(c -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), c));
    }
}
