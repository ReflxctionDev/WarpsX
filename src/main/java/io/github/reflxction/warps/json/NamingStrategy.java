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

import io.github.reflxction.warps.config.PlayerStoringStrategy;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.moltenjson.configuration.tree.strategy.TreeNamingStrategy;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class NamingStrategy implements TreeNamingStrategy<OfflinePlayer> {

    public static final TreeNamingStrategy<OfflinePlayer> INSTANCE = new NamingStrategy();

    /**
     * Converts the specified object to be a valid file name. The returned file name
     * should NOT contain the extension.
     *
     * @param e Object to convert
     * @return The valid file name.
     */
    @Override
    public String toName(@NotNull OfflinePlayer e) {
        return ((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).to(e);
    }

    /**
     * Converts the file name to be an object, can be used as a key.
     *
     * @param name The file name. This does <i>NOT</i> include the extension.
     * @return The object key
     */
    @Override
    public OfflinePlayer fromName(@NotNull String name) {
        return ((PlayerStoringStrategy) PluginSettings.STORING_STRATEGY.get()).from(name);
    }
}