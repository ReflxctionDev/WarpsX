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
package io.github.reflxction.warps.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.messages.MessageCategory;
import io.github.reflxction.warps.messages.MessageKey;
import io.github.reflxction.warps.util.compatibility.Compatibility;
import io.github.reflxction.warps.util.item.ItemFactory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@CommandAlias("warpsx")
public class WarpsXCommand extends BaseCommand implements Listener {

    private static final Material SIGN = Compatibility.getMaterial("SIGN", "OAK_SIGN");

    /* Page index controlling items*/

    private static final ItemStack PREVIOUS_PAGE = ItemFactory.create(Material.ARROW)
            .setName("&dPrevious Page").create();

    private static final ItemStack NEXT_PAGE = ItemFactory.create(Material.ARROW)
            .setName("&dNext Page").create();

    private static final Map<MessageCategory, Map<Integer, MessageKey>> KEYS = new HashMap<>();

    private static final String CANCEL = "cancel-edit";

    public static final WarpsXCommand INSTANCE = new WarpsXCommand();

    @Conditions("player")
    @Description("Display the messages GUI")
    @Default
    @CommandPermission("%admin.messages")
    @Subcommand("messages")
    public static void openGUI(CommandSender sender) {
        Inventory i = createGUI(MessageCategory.VALUES[0]);
        ((HumanEntity) sender).openInventory(i);
    }

    @Subcommand("reload")
    @CommandPermission("%admin.reload")
    @Syntax("&e[element to reload]")
    @Description("Reload a specific element")
    @CommandCompletion("@reloadable")
    public static void reload(CommandSender sender, @Optional @Default("config") @Values("config|warps-gui|messages") String target) {
        switch (target) {
            case "messages":
                MessageKey.load();
                Chat.admin(sender, "&aMessages have been reloaded");
                break;
            case "config":
                WarpsX.getPlugin().reloadConfig();
                Arrays.stream(PluginSettings.values).forEach(PluginSettings::request);
                Chat.admin(sender, "&aPlugin config has been reloaded");
                break;
            case "warps-gui":
                WarpsX.getPlugin().getWarpsGUI().refresh();
                Chat.admin(sender, "&aWarps GUI has been reloaded");
                break;
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        MessageCategory category = MessageCategory.fromTitle(event.getView().getTitle());
        if (category == null) return; // Not a message menu
        event.setCancelled(true);
        HumanEntity e = event.getWhoClicked();
        if (event.getRawSlot() == 27 && event.getInventory().getItem(27) != null) {
            Inventory i = createGUI(MessageCategory.VALUES[category.ordinal() - 1]);
            e.openInventory(i);
            return;
        }
        if (event.getRawSlot() == 35 && event.getInventory().getItem(35) != null) {
            Inventory i = createGUI(MessageCategory.VALUES[category.ordinal() + 1]);
            e.openInventory(i);
            return;
        }
        Map<Integer, MessageKey> categoryMap = KEYS.get(category);
        MessageKey key = categoryMap.get(event.getRawSlot());
        if (key == null) return; // Slot has no mapping
        if (event.getClick().name().contains("RIGHT")) {
            Chat.admin(e, "&eCurrent value for \"" + key.getName() + "\": &d" + key.getText());
            return;
        }
        e.closeInventory();
        e.setMetadata("spleefx.message-edit", new FixedMetadataValue(WarpsX.getPlugin(), key));
        Chat.admin(e, "&eType in the new value of &a\"&d" + key.getName() + "&a\"&e.");
        Chat.admin(e, "&eTo cancel, type &d" + CANCEL + "&e.");
    }

    @EventHandler(ignoreCancelled = true)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasMetadata("spleefx.message-edit")) {
            MessageKey key = (MessageKey) event.getPlayer().getMetadata("spleefx.message-edit").get(0).value();
            if (key == null) return;
            if (event.getMessage().equals(CANCEL)) {
                event.setCancelled(true);
                event.getPlayer().removeMetadata("spleefx.message-edit", WarpsX.getPlugin());
                Chat.admin(event.getPlayer(), "&aEditing has been cancelled.");
                return;
            }
            key.setText(Chat.colorize(event.getMessage()));
            event.setCancelled(true);
            Chat.admin(event.getPlayer(), "&aValue of &e" + key.getName() + " &ahas been changed to &d" + event.getMessage());
            event.getPlayer().removeMetadata("spleefx.message-edit", WarpsX.getPlugin());
        }
    }

    private static Inventory createGUI(MessageCategory category) {
        Inventory inventory = Bukkit.createInventory(null, 36, category.getTitle());
        KEYS.get(category).forEach((slot, key) -> inventory.setItem(slot, create(key.getName(), key.getDescription())));
        if (category.ordinal() > 0)
            inventory.setItem(27, PREVIOUS_PAGE);
        if (category.ordinal() != MessageCategory.VALUES.length - 1) // If it is not last page
            inventory.setItem(35, NEXT_PAGE);
        return inventory;
    }

    private static ItemStack create(String name, String desc) {
        return ItemFactory.create(SIGN)
                .setName("&a" + name).setLore(ChatColor.YELLOW + desc, "", "&dLeft click &eto edit", "&dRight click &eto view current value.").create();
    }

    static {
        for (MessageCategory category : MessageCategory.VALUES) {
            int i = -1;
            Map<Integer, MessageKey> map = new HashMap<>();
            for (MessageKey key : MessageKey.byCategory(category)) {
                map.put(i++ == 27 ? (i = 28) : i, key);
            }
            KEYS.put(category, map);
        }
    }

}
