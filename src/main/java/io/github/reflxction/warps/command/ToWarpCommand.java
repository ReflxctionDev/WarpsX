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
import io.github.reflxction.warps.api.WarpUseEvent;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.json.PluginData;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.messages.MessageKey;
import io.github.reflxction.warps.safety.SafetyViolations;
import io.github.reflxction.warps.util.compatibility.Commands;
import io.github.reflxction.warps.util.game.delay.DelayData;
import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Set;

@CommandAlias("warp")
public class ToWarpCommand extends BaseCommand {

    private static final Permission WARP = new Permission("warpsx.warp", PermissionDefault.TRUE);

    @Default
    @Syntax("&a<warp> &7[confirmation]")
    @Conditions("player") @CommandCompletion("@playerwarps @confirmation") @Description("Teleport to a warp")
    public static void warpTo(CommandSender sender, PlayerWarp warp, @Optional @Default("false") boolean confirmation) {
        if (!Commands.permission(sender, WARP)) {
            Chat.permission(sender);
            return;
        }
        Player player = Commands.safe(sender);
        if (WarpsX.getPluginData().getBannedUsers().contains(player.getUniqueId())) {
            MessageKey.USER_BANNED.send(sender, warp.getLocation(), warp, player);
            return;
        }
        if (warp.isBanned(player)) {
            MessageKey.BANNED_FROM_WARP.send(sender, warp.getLocation(), warp, player);
            return;
        }
        if (warp.isWarpBanned() && !sender.hasPermission("warpsx.bypass.warpban")) {
            MessageKey.WARP_BANNED.send(sender, warp.getLocation(), warp, player);
            return;
        }
        if (!warp.isInvited(player)) {
            MessageKey.NOT_INVITED.send(sender, warp.getLocation(), warp, player);
            return;
        }
        Set<SafetyViolations> violations = SafetyViolations.getSafetyViolations(warp, PluginSettings.SAFETY_RADIUS.get());
        // There are violations
        if (!violations.isEmpty() && !confirmation) {
            MessageKey.WARP_NOT_SAFE.send(sender, warp.getLocation(), warp, player);
            Chat.admin(sender, "&eViolations: ");
            violations.stream().map(SafetyViolations::getDescription).forEach(v -> Chat.unprefixed(sender, "&7- &e" + v));
            return;
        }
        if (warp.getDelay() != 0) {
            int delay = WarpsX.getPlugin().getDelayExecutor().getTimeLeft(player, "warp=" + warp);
            if (delay != 0) {
                if (delay <= 60) {
                    MessageKey.MUST_WAIT.send(sender, warp.getLocation(), warp, player);
                    return;
                }
                int hours = delay / 3600;
                int minutes = (delay % 3600) / 60;
                int seconds = delay % 60;
                String m = (hours == 0 ? "" : hours + " hour" + plural(hours) + " ") + (minutes == 0 ? "" : minutes + " minute" + plural(minutes) + " ") + (seconds == 0 ? "" : seconds);
                MessageKey.MUST_WAIT.send(sender, warp.getLocation(), warp, player, "{player_delay}", m, "{player_delay_plural}", plural(seconds));
                return;
            }
            WarpsX.getPlugin().getDelayExecutor().setDelay(player, "warp=" + warp.getKey(), new DelayData(warp.getDelay()));
        }
        player.teleport(warp.getLocation());
        WarpUseEvent warpUseEvent = new WarpUseEvent(player, warp);
        Bukkit.getPluginManager().callEvent(warpUseEvent);
        if (warp.getSound() != null)
            player.playSound(player.getLocation(), warp.getSound(), 1, 1);
        if (warp.getPotionEffects() != null) warp.getPotionEffects().forEach(player::addPotionEffect);
        if (!warp.getGreetingMessage().equals("{}"))
            Chat.unprefixed(sender, warp.getGreetingMessage().replace("{warp}", warp.getKey()));
    }

    private static String plural(int v) {
        return v <= 1 ? "" : "s";
    }

}