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
package io.github.reflxction.warps.json;

import io.github.moltenjson.configuration.select.SelectKey;
import io.github.moltenjson.configuration.select.SelectionHolder;
import org.bukkit.OfflinePlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Settings controlled by commands
 */
public class PluginData {

    @SelectKey("adminOnly")
    public static final SelectionHolder<Boolean> ADMIN_ONLY = new SelectionHolder<>(true);

    @SelectKey("warpsLimit")
    public static final SelectionHolder<Integer> WARPS_LIMIT = new SelectionHolder<>(15);

    @SelectKey("bannedPlayers")
    public static final SelectionHolder<Set<UUID>> BANNED_USERS = new SelectionHolder<>(new HashSet<>());

    @SelectKey("bannedWarpOwners")
    public static final SelectionHolder<Set<UUID>> BANNED_WARP_OWNERS = new SelectionHolder<>(new HashSet<>());

}