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
import org.bukkit.command.CommandSender;

/**
 * A little chat utility
 */
public class Chat {

    /**
     * Sends a message with the plugin prefix
     *
     * @param sender  Sender to send to
     * @param message Message to send
     */
    public static void plugin(CommandSender sender, String message) {
        sender.sendMessage(MessageKey.prefix() + colorize(message));
    }

    /**
     * Sends a message with the plugin prefix
     *
     * @param sender  Sender to send to
     * @param message Message to send
     */
    public static void admin(CommandSender sender, String message) {
        sender.sendMessage(MessageKey.prefix() + colorize(message));
    }

    /**
     * Sends a no-permission message with the plugin prefix
     *
     * @param sender Sender to send to
     */
    public static void permission(CommandSender sender) {
        MessageKey.NO_PERMISSION.send(sender, null, null, null);
    }

    /**
     * Sends a message with no prefix
     *
     * @param sender  Sender to send to
     * @param message Message to send
     */
    public static void unprefixed(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Colors the specified string
     *
     * @param text String to color
     * @return The colored string
     */
    public static String colorize(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

}