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
import io.github.reflxction.warps.gui.WarpGUI;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.util.compatibility.Commands;
import io.github.reflxction.warps.warp.PlayerWarp;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

@CommandAlias("warpgui|warpsgui|wgui")
public class WarpsGUICommand extends BaseCommand {

    private static final Permission GUI = new Permission("warpsx.gui", PermissionDefault.TRUE);

    @Default
    @Syntax("&a[warp key]")
    @Conditions("player") @CommandCompletion("@playerwarps") @Description("Teleport to a warp")
    public static void display(CommandSender sender, @Optional PlayerWarp warp) {
        if (!Commands.permission(sender, GUI)) {
            Chat.permission(sender);
            return;
        }
        Player player = Commands.safe(sender);
        if (warp == null) {
            WarpGUI.displayAllWarps(player);
            return;
        }
        WarpGUI.displayWarp(player, warp);
    }

}