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
package io.github.reflxction.warps.util;

import org.apache.commons.io.FileUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * A utility to simplify managing files
 */
public class FileManager<P extends JavaPlugin> {

    /**
     * Main class instance
     */
    private P plugin;

    public FileManager(P plugin) {
        this.plugin = plugin;
    }

    /**
     * Creates a replica of an embedded file
     *
     * @param fileName File name to create
     * @return The file instance
     */
    public File createFile(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists())
            plugin.saveResource(fileName, false);
        return file;
    }

    /**
     * Creates the specified directory
     *
     * @param directory Directory to create
     */
    public File createDirectory(String directory) {
        File file = new File(plugin.getDataFolder(), directory);
        try {
            FileUtils.forceMkdir(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    public P getPlugin() {
        return plugin;
    }
}
