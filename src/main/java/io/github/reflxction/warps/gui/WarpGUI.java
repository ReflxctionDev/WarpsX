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
package io.github.reflxction.warps.gui;

import com.google.gson.annotations.Expose;
import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.command.ToWarpCommand;
import io.github.reflxction.warps.command.WarpsCommand;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.util.compatibility.Commands;
import io.github.reflxction.warps.util.item.ItemFactory;
import io.github.reflxction.warps.util.item.ItemHolder;
import io.github.reflxction.warps.warp.PlayerWarp;
import io.github.reflxction.warps.warp.WarpController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;

public class WarpGUI implements Listener {

    @Expose
    private AllWarpsMenu allWarpsMenu = null;

    @Expose
    private Menu warpsMenu = null;

    private static final Map<IntPredicate, Integer> SLOT_SIZE = new HashMap<>();
    private static final Entry<IntPredicate, Integer> INVALID_SIZE = new SimpleEntry<>(null, 6);

    private static int getAppropriateSize(int size) {
        Optional<Entry<IntPredicate, Integer>> i = SLOT_SIZE.entrySet().stream().filter(e -> e.getKey().test(size)).findFirst();
        return i.orElse(INVALID_SIZE).getValue() * 9;
    }

    @SuppressWarnings("unchecked")
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = Commands.safe(event.getWhoClicked());
        if (player.hasMetadata("warpsx.display")) {
            Map<Integer, PlayerWarp> warpMap = (Map<Integer, PlayerWarp>) player.getMetadata("warpsx.display").get(0).value();
            if (warpMap == null) return; // Shouldn't happen but these warnings are so annoying smh
            PlayerWarp clickedWarp = warpMap.get(event.getRawSlot());
            if (clickedWarp == null) return;
            event.setCancelled(true);
            displayWarp(player, clickedWarp);
            return;
        }
        if (player.hasMetadata("warpsx.warpgui")) {
            event.setCancelled(true);
            GuiItem warpTo = warpsMenu.getItems().get("goToWarp");
            GuiItem deleteWarp = warpsMenu.getItems().get("deleteWarp");
            GuiItem changeLoc = warpsMenu.getItems().get("changeLocation");
            PlayerWarp warp = (PlayerWarp) player.getMetadata("warpsx.warpgui").get(0).value();
            if (event.getSlot() == warpTo.getSlot()) removeThen(player, (c) -> ToWarpCommand.warpTo(c, warp, false));
            if (event.getSlot() == deleteWarp.getSlot()) removeThen(player, (c) -> WarpsCommand.deleteWarp(c, warp));
            if (event.getSlot() == changeLoc.getSlot()) removeThen(player, (c) -> WarpsCommand.changeLocation(c, warp));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        event.getPlayer().removeMetadata("warpsx.display", WarpsX.getPlugin());
        event.getPlayer().removeMetadata("warpsx.warpgui", WarpsX.getPlugin());
    }

    private static void removeThen(Player player, Consumer<Player> task) {
        task.accept(player);
        player.removeMetadata("warpsx.warpgui", WarpsX.getPlugin());
        player.closeInventory();
    }

    public static void displayAllWarps(Player player) {
        Map<String, PlayerWarp> warps = WarpController.getWarps(player);
        Inventory inventory = Bukkit.createInventory(null, getAppropriateSize(warps.size()), WarpsX.getWarpGUI().allWarpsMenu.getTitle(player));
        List<PlayerWarp> warpList = new ArrayList<>(warps.values());
        Map<Integer, PlayerWarp> warpsMap = new HashMap<>();
        for (int i = 0; i < warpList.size(); i++) {
            PlayerWarp warp = warpList.get(i);
            ItemStack item = applyPlaceholders(WarpsX.getWarpGUI().allWarpsMenu.getWarpItem(), warp);
            warpsMap.put(i, warp);
            inventory.setItem(i, item);
        }
        player.openInventory(inventory);
        player.setMetadata("warpsx.display", new FixedMetadataValue(WarpsX.getPlugin(), warpsMap));
    }

    public static void displayWarp(Player player, PlayerWarp warp) {
        Menu menu = WarpsX.getWarpGUI().warpsMenu;
        Inventory i = Bukkit.createInventory(null, menu.getRows() * 9, placeholders(menu.getTitle(), warp));
        GuiItem warpTo = menu.getItems().get("goToWarp");
        GuiItem deleteWarp = menu.getItems().get("deleteWarp");
        GuiItem changeLoc = menu.getItems().get("changeLocation");
        i.setItem(warpTo.getSlot(), applyPlaceholders(warpTo.getItem(), warp));
        i.setItem(deleteWarp.getSlot(), applyPlaceholders(deleteWarp.getItem(), warp));
        i.setItem(changeLoc.getSlot(), applyPlaceholders(changeLoc.getItem(), warp));
        player.openInventory(i);
        player.removeMetadata("warpsx.display", WarpsX.getPlugin());
        player.setMetadata("warpsx.warpgui", new FixedMetadataValue(WarpsX.getPlugin(), warp));
    }

    private static ItemStack applyPlaceholders(ItemFactory item, PlayerWarp warp) {
        ItemMeta current = item.create().getItemMeta();
        if (current != null) {
            item.setName(placeholders(current.getDisplayName(), warp));
            if (current.getLore() != null)
                item.setLore(current.getLore().stream().map(s -> placeholders(s, warp)).collect(Collectors.toList()));
        }
        return item.create();
    }

    private static String placeholders(String string, PlayerWarp warp) {
        return string
                .replace("{warp_key}", warp.getKey())
                .replace("{warp_greeting}", warp.getGreetingMessage())
                .replace("{warp_delay}", Integer.toString(warp.getDelay()));
    }

    static {
        SLOT_SIZE.put((v) -> isBetween(0, 9, v), 1);
        SLOT_SIZE.put((v) -> isBetween(10, 18, v), 2);
        SLOT_SIZE.put((v) -> isBetween(19, 27, v), 3);
        SLOT_SIZE.put((v) -> isBetween(28, 36, v), 4);
        SLOT_SIZE.put((v) -> isBetween(37, 45, v), 5);
        SLOT_SIZE.put((v) -> isBetween(46, 54, v), 6);
        SLOT_SIZE.put((v) -> isBetween(55, 63, v), 7);
    }

    private static boolean isBetween(int a, int b, int test) {
        return test >= a && test <= b;
    }

    public static class AllWarpsMenu {

        @Expose
        private String title;

        @Expose
        private ItemHolder warpItem;

        public String getTitle(Player player) {
            return Chat.colorize(title).replace("{player}", player.getName());
        }

        public ItemFactory getWarpItem() {
            return warpItem.factory();
        }
    }

}