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
package io.github.reflxction.warps.util.chat;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.util.chat.ChatComponent.Adapter;
import io.github.reflxction.warps.util.compatibility.Protocol;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static io.github.reflxction.warps.util.compatibility.Protocol.getCraftBukkitClass;
import static io.github.reflxction.warps.util.compatibility.Protocol.getProtocolClass;

/**
 * Represents a JSON text message.
 */
public class ComponentJSON {

    /**
     * The GSON used for serializing and deserializing
     */
    private static final Gson CHAT_GSON = new GsonBuilder().registerTypeAdapter(ChatComponent.class, new Adapter()).create();

    private static boolean disable = false;

    /**
     * A list which contains all JSON components
     */
    @Expose
    private List<ChatComponent> components = new LinkedList<>();

    /**
     * Appends a {@link ChatComponent} so it gets included in the parent message components
     *
     * @param component Component to append
     * @return A reference to this builder
     */
    public ComponentJSON append(ChatComponent component) {
        components.add(component);
        return this;
    }

    /**
     * Appends a space
     *
     * @return A reference to this builder
     */
    public ComponentJSON space() {
        return append(ChatComponent.SPACE);
    }

    /**
     * Clears all text components inside this holder
     *
     * @return A reference to this builder
     */
    public ComponentJSON clear() {
        components.clear();
        return this;
    }

    /**
     * Returns the JSON string representing this message
     *
     * @return The JSON string
     */
    @Override
    public String toString() {
        return CHAT_GSON.toJson(components);
    }

    /**
     * Returns the stripped message
     *
     * @return The message with no events
     */
    public String getStripped() {
        StringBuilder b = new StringBuilder();
        components.forEach(c -> b.append(c.getText()));
        return b.toString();
    }

    /**
     * Sends this chat component to the sender
     *
     * @param sender Sender to send to
     */
    public ComponentJSON send(CommandSender sender) {
        if (disable || !(sender instanceof Player)) sender.sendMessage(Chat.colorize(getStripped()));
        else {
            try {
                Object entitysender = getHandle.invoke(sender);
                Object packet = packetPlayOutChat.newInstance(serialize.invoke(null, toString()));
                sendPacket.invoke(playerConnection.get(entitysender), packet);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    private static Method getHandle, serialize, sendPacket;
    private static Constructor packetPlayOutChat;
    private static Field playerConnection;

    static {
        try {
            getHandle = Protocol.method(getCraftBukkitClass("entity.CraftPlayer"), "getHandle");
            serialize = Protocol.method(getProtocolClass("IChatBaseComponent$ChatSerializer"), "a", String.class);
            sendPacket = Protocol.method(getProtocolClass("PlayerConnection"), "sendPacket", getProtocolClass("Packet"));
            playerConnection = getProtocolClass("EntityPlayer").getDeclaredField("playerConnection");
            packetPlayOutChat = getProtocolClass("PacketPlayOutChat").getDeclaredConstructor(getProtocolClass("IChatBaseComponent"));
        } catch (ReflectiveOperationException e) {
            WarpsX.getPlugin().getLogger().warning("Failed to access required NMS data to send titles. Titles will not be sent");
            disable = true;
        }
    }
}