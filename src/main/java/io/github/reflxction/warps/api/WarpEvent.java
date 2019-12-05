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
import org.bukkit.event.Event;

/**
 * Represents a warp event
 */
public abstract class WarpEvent extends Event {

    /**
     * The warp of the event
     */
    private PlayerWarp warp;

    /**
     * Initiates a new warp event
     *
     * @param warp The warp
     */
    public WarpEvent(PlayerWarp warp) {
        this.warp = warp;
    }

    /**
     * Returns the warp of the event
     *
     * @return The warp of the event
     */
    public PlayerWarp getWarp() {
        return warp;
    }

}
