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

import com.google.common.collect.Sets;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enums that indicate whether a warp is safe or not
 */
public enum SafetyViolations {

    /**
     * The warp contains lava
     */
    LAVA("Lava/Fire"),

    /**
     * The warp contains TNT
     */
    TNT("TNT"),

    /**
     * Using the warp would lead to suffocation damage
     */
    SUFFOCATION("Suffocation (no air)"),

    /**
     * No blocks in the warp (entirely air/water)
     */
    NO_BLOCKS("No blocks"),

    /**
     * There is no solid block in the warp location
     */
    NO_PLATFORM("No platform");

    private String description;

    SafetyViolations(String description) {
        this.description = description;
    }

    /**
     * Returns the description of the safety violation
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a list of all safety violations of the warp
     *
     * @param warp         Warp to check
     * @param cuboidLength Length of the cuboid to check within
     * @return A set of all violations
     */
    public static Set<SafetyViolations> getSafetyViolations(PlayerWarp warp, int cuboidLength) {
        if (!((boolean) PluginSettings.SAFETY_WARN.get())) return Collections.emptySet();
        Set<SafetyViolations> safety = new HashSet<>();
        cuboidLength = getLength(cuboidLength);
        Location loc = warp.getLocation().clone();
        Location a = new Location(loc.getWorld(), loc.getX() + cuboidLength, loc.getY() + cuboidLength, loc.getZ() + cuboidLength);
        Location b = new Location(loc.getWorld(), loc.getX() - cuboidLength, loc.getY() - cuboidLength, loc.getZ() - cuboidLength);
        CuboidProtection cuboid = new CuboidProtection(a, b);
        List<Material> materials = cuboid.getBlocks().stream().map(Block::getType).collect(Collectors.toList());
        Location head = loc.clone().add(0, 1, 0);
        if (!materials.contains(Material.AIR) || head.getBlock().getType().isSolid() || loc.getBlock().getType().isSolid())
            safety.add(SUFFOCATION); // Suffocate

        if (materials.contains(Material.TNT))
            safety.add(TNT);

        if (materials.contains(Material.FIRE) || materials.contains(Material.LAVA) || materials.contains(Material.matchMaterial("STATIONARY_LAVA")))
            safety.add(LAVA); // Lava

        if (!loc.subtract(0, 1, 0).getBlock().getType().isSolid())
            safety.add(NO_PLATFORM); //

        Set<Material> m = new HashSet<>(materials);
        if (m.size() == 1 && m.contains(Material.AIR))
            safety.add(NO_BLOCKS);
        return Sets.intersection(safety, PluginSettings.SAFETY_CHECKS.requestEnumSet(SafetyViolations.class));
    }

    private static int getLength(int x) {
        return x % 2 == 0 ? getLength(x - 1) : (x - 1) / 2 + 1;
    }

}
