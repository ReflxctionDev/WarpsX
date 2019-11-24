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
package io.github.reflxction.warps.util.compatibility;

import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.function.Supplier;

/**
 * A utility class for providing compatibility to changing names of enums
 */
public class Compatibility {

    /**
     * Returns the first object if available, or the second if the first does not exist
     * <p>
     * Used to provide compatibility for newer and legacy methods
     *
     * @param a   First object
     * @param b   Second object in case the first fails
     * @param <R> The object returned
     * @return The appropriate object
     */
    public static <R> R either(Supplier<R> a, Supplier<R> b) {
        try {
            return a.get();
        } catch (NoSuchMethodError | NoSuchFieldError e) {
            return b.get();
        }
    }

    /**
     * Returns the sound from the name(s)
     *
     * @param lookup Names to look up with.
     * @return The first available sound with the name
     */
    public static Sound getSound(String... lookup) {
        for (String name : lookup) {
            try {
                return Sound.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return null;
    }

    /**
     * Returns the material from the name(s)
     *
     * @param lookup Names to look up with.
     * @return The first available material with the name
     */
    public static Material getMaterial(String... lookup) {
        for (String name : lookup) {
            Material m = Material.matchMaterial(name.toLowerCase());
            if (m == null) continue;
            return m;
        }
        return null;
    }

}
