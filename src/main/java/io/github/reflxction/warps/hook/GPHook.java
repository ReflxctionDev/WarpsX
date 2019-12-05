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
package io.github.reflxction.warps.hook;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Hook for GriefPrevention
 */
@PluginHook(requiredPlugin = "GriefPrevention")
public class GPHook {

    private static Claim getClaimAtLocation(final Location loc) {
        return GriefPrevention.instance.dataStore.getClaimAt(loc, false, null);
    }

    public static boolean isOwnerAtLocation(final Player p, final Location loc) {
        PlayerData d = GriefPrevention.instance.dataStore.getPlayerData(p.getUniqueId());
        if (d.ignoreClaims) return true;
        Claim claim = getClaimAtLocation(loc);
        return claim == null || claim.getOwnerName().equals(p.getName());
    }
}