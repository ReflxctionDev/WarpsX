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
package io.github.reflxction.warps.hook;

import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.api.WarpCreateEvent;
import io.github.reflxction.warps.api.WarpUseEvent;
import io.github.reflxction.warps.warp.PlayerWarp;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;

import static org.bukkit.Bukkit.getServer;

/**
 * Hook for Vault
 */
@PluginHook(requiredPlugin = "Vault")
public class VaultHook implements Listener {

    /**
     * The server economy
     */
    private Economy economy;

    public VaultHook() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp =
                    getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp == null) return;
            economy = rsp.getProvider();
        }
    }

    private void charge(Player player, int value) {
        if (economy != null)
            economy.withdrawPlayer(player, value);
    }

    @EventHandler(ignoreCancelled = true)
    public void onWarpUse(WarpUseEvent event) {
        PlayerWarp warp = event.getWarp();
        if (warp.getUseCost() <= 0) return;
        charge(event.getPlayer(), warp.getUseCost());
    }

    @EventHandler(ignoreCancelled = true)
    public void onWarpCreate(WarpCreateEvent event) {
        if (WarpsX.getPluginData().getWarpCreationCost() <= 0) return;
        charge(event.getCreator(), WarpsX.getPluginData().getWarpCreationCost());
    }

    public Economy getEconomy() {
        return economy;
    }

}
