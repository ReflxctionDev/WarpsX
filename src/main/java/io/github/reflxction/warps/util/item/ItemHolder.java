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
package io.github.reflxction.warps.util.item;

import com.google.gson.annotations.Expose;
import io.github.reflxction.warps.util.compatibility.Protocol;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * A class which holds data for an {@link org.bukkit.inventory.ItemStack}
 */
public class ItemHolder {

    @Expose
    private String type;

    @Expose
    private int count;

    @Expose
    private Map<Enchantment, Integer> enchantments;

    @Expose
    private String displayName;

    @Expose
    private List<String> lore;

    @Expose
    private List<ItemFlag> itemFlags;

    @Expose
    private String color;

    public ItemFactory factory() {
        ItemStack item;
        DyeColor color = this.color == null ? null : DyeColor.valueOf(this.color.toUpperCase());
        if (color != null)
            if (Protocol.isNewerThan(13)) // The flattening, in 1.13+
                item = new ItemStack(Material.matchMaterial(color.name() + "_" + type.toUpperCase()));
            else
                item = new ItemStack(Material.matchMaterial(type.toUpperCase()), 1, color.getWoolData());
        else
            item = new ItemStack(Material.matchMaterial(type.toUpperCase()));
        ItemFactory f = ItemFactory.create(item).setAmount(count).addEnchantments(enchantments)
                .setLore(lore).addItemFlags(itemFlags);
        if (!displayName.equals("{}")) f.setName(displayName);
        return f;
    }

}