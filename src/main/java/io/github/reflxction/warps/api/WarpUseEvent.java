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
package io.github.reflxction.warps.api;

import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Fired when a warp is used
 */
public class WarpUseEvent extends WarpEvent {

    private static final HandlerList handlers = new HandlerList();

    private Player player;

    /**
     * Creates a new {@link WarpUseEvent}.
     *
     * @param player The warp user
     * @param warp   The warp that was used
     */
    public WarpUseEvent(Player player, PlayerWarp warp) {
        super(warp);
        this.player = player;
    }

    /**
     * Returns the player who used the warp
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
