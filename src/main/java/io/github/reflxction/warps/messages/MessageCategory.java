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
package io.github.reflxction.warps.messages;

import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a message category
 */
public enum MessageCategory {

    /**
     * Success messages category
     */
    SUCCESS(ChatColor.DARK_GREEN + "Success Messages"),

    /**
     * Error messages
     */
    ERROR(ChatColor.DARK_RED + "Error Messages");

    /**
     * Values of all categories
     */
    public static final MessageCategory[] VALUES = values();

    /**
     * The GUI title for this category
     */
    private String title;

    /**
     * Map of all categories by their titles
     */
    private static final Map<String, MessageCategory> BY_TITLE = new HashMap<>();

    MessageCategory(String title) {
        this.title = title;
    }

    /**
     * Returns the GUI title of the category
     *
     * @return The GUI title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Retrieves a {@link MessageCategory} from the specified title
     *
     * @param title Title to fetch from
     * @return The category, or {@code null} if invalid.
     */
    public static MessageCategory fromTitle(String title) {
        return BY_TITLE.get(title);
    }

    static {
        Arrays.stream(VALUES).forEach(c -> BY_TITLE.put(c.title, c));
    }

}
