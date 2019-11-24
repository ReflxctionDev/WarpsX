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
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import io.github.reflxction.warps.config.PluginSettings;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.messages.MessageKey;
import io.github.reflxction.warps.safety.SafetyViolations;
import io.github.reflxction.warps.util.compatibility.Commands;
import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.Set;

public class WarpSafetyCommand extends BaseCommand {

    private static final Permission CHECK_SAFETY = new Permission("warpsx.checksafety", PermissionDefault.TRUE);

    @CommandAlias("warpsafety|checkloc")
    @CommandCompletion("@playerwarps")
    @Description("Check whether a warp location is safe or not")
    @Syntax("&e<warp key>")
    public static void getSafety(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, CHECK_SAFETY)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender) && (!(sender instanceof Player) || !warp.isInvited((Player) sender))) {
            MessageKey.NOT_INVITED.send(sender, warp.getLocation(), warp, null);
            return;
        }
        Set<SafetyViolations> violations = SafetyViolations.getSafetyViolations(warp, PluginSettings.SAFETY_RADIUS.get());
        // There are violations
        if (violations.isEmpty()) {
            MessageKey.WARP_SAFE.send(sender, warp.getLocation(), warp, null);
            return;
        }
        Chat.admin(sender, "&eViolations: ");
        violations.stream().map(SafetyViolations::getDescription).forEach(v -> Chat.unprefixed(sender, "&7- &e" + v));
    }

}
