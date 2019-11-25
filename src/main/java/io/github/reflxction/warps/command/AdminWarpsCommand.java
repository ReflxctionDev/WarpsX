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
import co.aikar.commands.CommandHelp;
import co.aikar.commands.HelpEntry;
import co.aikar.commands.annotation.*;
import io.github.reflxction.warps.json.PluginData;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.util.chat.ChatComponent;
import io.github.reflxction.warps.util.chat.ChatEvents.ClickEvent;
import io.github.reflxction.warps.util.chat.ChatEvents.HoverEvent;
import io.github.reflxction.warps.util.chat.ComponentJSON;
import io.github.reflxction.warps.util.chat.PageFooter;
import io.github.reflxction.warps.util.game.ListPaginator;
import io.github.reflxction.warps.util.game.ListPaginator.Footer;
import io.github.reflxction.warps.util.game.ListPaginator.Header;
import io.github.reflxction.warps.util.game.ListPaginator.MessageConverter;
import io.github.reflxction.warps.util.game.ListPaginator.MessagePlatform;
import io.github.reflxction.warps.warp.PlayerWarp;
import io.github.reflxction.warps.warp.WarpController;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@CommandAlias("awarps|warpsadmin")
public class AdminWarpsCommand extends BaseCommand {

    private static final ComponentJSON JSON = new ComponentJSON();

    private static final MessagePlatform<ChatComponent> PLATFORM = (sender, entry) -> JSON.clear().append(entry).send(sender).clear();

    private static final MessageConverter<HelpEntry, ChatComponent> CONVERTER = (entry) -> {
        String message = "&b/" + entry.getCommand() + " " + entry.getParameterSyntax() + " &7- &a" + entry.getDescription().replace(" ", " &a");
        return new ChatComponent().setText(message)
                .setClickAction(ClickEvent.SUGGEST_COMMAND, "/" + entry.getCommand() + " ")
                .setHoverAction(HoverEvent.SHOW_TEXT, "Click to add to your chat box");
    };

    private static final Header HEADER = (sender, pageIndex, pageCount) -> Chat.unprefixed(sender, "&c&m------&r&e Page &a(" + (pageIndex == pageCount ? "&c" : "&7") + pageIndex + " &9/ &c" + (pageCount) + "&a) &c&m------");
    private static final Footer FOOTER = new PageFooter("warpsadmin", JSON);

    private static final ListPaginator<HelpEntry, ChatComponent> PAGINATOR = new ListPaginator<>(7, PLATFORM, CONVERTER)
            .setHeader(HEADER).setFooter(FOOTER).ifPageIsInvalid((sender, page) -> Chat.admin(sender, "&cInvalid help page: &e" + page));

    @Subcommand("help|?|-help|-h|-?")
    @CommandCompletion("@range:1-3")
    @Syntax("&e<page number>")
    @Default @CatchUnknown
    @CommandPermission("warpsx.admin.help")
    @Description("Display help page")
    public void onHelp(CommandSender sender, @Optional @Default("1") int page) {
        if (page == 0) page = 1;
        CommandHelp commandHelp = getCommandHelp();
        PAGINATOR.sendPage(commandHelp.getHelpEntries(), sender, page);
    }

    @Subcommand("delay")
    @Syntax("&e<warp key> <delay>")
    @Description("Set the delay of a warp")
    @CommandPermission("%admin.delay")
    @CommandCompletion("@playerwarps @range:1-20")
    public static void delay(CommandSender sender, PlayerWarp warp, @Default("10") int delay) {
        warp.setDelay(delay);
        Chat.admin(sender, "&aDelay for warp &e" + warp.getKey() + " &ais now &e" + delay + "&a.");
    }

    @Subcommand("cue|sound")
    @Syntax("&e<warp key> <sound>")
    @Description("Set the cue sound of the warp")
    @CommandPermission("%admin.cue")
    @CommandCompletion("@playerwarps @sounds")
    public static void setSound(CommandSender sender, PlayerWarp warp, Sound sound) {
        warp.setSound(sound);
        Chat.admin(sender, "&aSound for warp &e" + warp.getKey() + " &ahas been set to &e" + sound.name().toLowerCase());
    }

    @Subcommand("removecue|removesound")
    @Syntax("&e<warp key>")
    @Description("Set the cue sound of the warp")
    @CommandPermission("%admin.removesound")
    @CommandCompletion("@playerwarps")
    public static void removeSound(CommandSender sender, PlayerWarp warp) {
        warp.setSound(null);
        Chat.admin(sender, "&aSound for warp &e" + warp.getKey() + " &ahas been removed");
    }

    @CommandPermission("%admin.effects.add")
    @Syntax("&e<warp key> &e<effect>:<duration>:<amplifier>")
    @Description("Add potion effects that players will be given when this warp is used")
    @CommandCompletion("@playerwarps @nothing")
    @Subcommand("effects add")
    public static void addEffects(CommandSender sender, PlayerWarp warp, String[] args) {
        for (String e : args) {
            PotionEffect effect = parseEffect(e);
            if (effect == null) {
                Chat.admin(sender, "&cInvalid effect: &e" + e);
                continue;
            }
            warp.getPotionEffects().add(effect);
            Chat.admin(sender, "&aSuccessfully added effect " +
                    "&e" + effect.getType().getName().toLowerCase() + " " +
                    "&awith duration &e" + effect.getDuration() + " &aand amplifier &e" + effect.getAmplifier() + "&a.");
        }
    }

    @Subcommand("effects clear")
    @CommandPermission("%admin.effects.clear")
    @Syntax("&e<warp key>")
    @Description("Clear all potion effects that will be given to this warp")
    @CommandCompletion("@playerwarps")
    public static void clearEffects(CommandSender sender, PlayerWarp warp) {
        warp.getPotionEffects().clear();
        Chat.admin(sender, "&aEffects for warp &e" + warp.getKey() + " &ahave been cleared.");
    }

    @Subcommand("effects remove")
    @CommandPermission("%admin.effects.remove")
    @Syntax("&e<warp key> <effect type>")
    @Description("Remove specific potion effects that are added to this warp when it is used")
    @CommandCompletion("@playerwarps @effecttypes @nothing")
    public static void removeEffects(CommandSender sender, PlayerWarp warp, String[] args) {
        for (String e : args) {
            PotionEffectType effect = PotionEffectType.getByName(e);
            if (effect == null) {
                Chat.admin(sender, "&cInvalid effect: &e" + e);
                continue;
            }
            if (warp.getPotionEffects().removeIf(ef -> ef.getType().equals(effect))) {
                Chat.admin(sender, "&aEffect &e" + effect.getName().toLowerCase() + " &ahas been removed.");
            }
        }
    }

    @Subcommand("clearwarps|removeallwarps")
    @CommandPermission("%admin.warps.clearall")
    @Syntax("&e<player to clear for>")
    @Description("Clear all warps for a specific user")
    @CommandCompletion("@players")
    public static void removeAllWarps(CommandSender sender, OfflinePlayer player) {
        WarpController.getWarpKeys(player).forEach(WarpController::removeWarp);
        Chat.admin(sender, "&aSuccessfully cleared all warps for player &e" + player.getName());
    }

    @Subcommand("ban")
    @CommandPermission("%admin.ban.user")
    @Syntax("&e<player to ban>")
    @Description("Ban a player from using warps")
    @CommandCompletion("@players")
    public static void banUser(CommandSender sender, OfflinePlayer player) {
        PluginData.BANNED_USERS.get().add(player.getUniqueId());
        Chat.admin(sender, "&aPlayer &e" + player.getName() + " &acan no longer &cuse &aany warps.");
    }

    @Subcommand("unban")
    @CommandPermission("%admin.unban.user")
    @Syntax("&e<player to unban>")
    @Description("Unban a player from using warps")
    @CommandCompletion("@players")
    public static void unbanUser(CommandSender sender, OfflinePlayer player) {
        PluginData.BANNED_USERS.get().remove(player.getUniqueId());
        Chat.admin(sender, "&aPlayer &e" + player.getName() + " &acan now use warps again.");
    }

    @Subcommand("unbanall")
    @CommandPermission("%admin.unban.user.all")
    @Syntax("")
    @Description("Unban all players banned from using warps")
    @CommandCompletion("@players")
    public static void unbanAll(CommandSender sender) {
        PluginData.BANNED_USERS.get().clear();
        Chat.admin(sender, "&aSuccessfully unbanned all players banned from using warps.");
    }

    @Subcommand("banwarp")
    @CommandPermission("%admin.ban.warp")
    @Syntax("&e<warp key>")
    @Description("Bans a warp from being used")
    @CommandCompletion("@playerwarps")
    public static void banWarp(CommandSender sender, PlayerWarp warp) {
        warp.setBanned(true);
        Chat.admin(sender, "&aWarp &e" + warp.getKey() + " &ahas been banned.");
    }

    @Subcommand("unbanwarp")
    @CommandPermission("%admin.unban.warp")
    @Syntax("&e<warp key>")
    @Description("Unbans a warp from being used")
    @CommandCompletion("@playerwarps")
    public static void unbanWarp(CommandSender sender, PlayerWarp warp) {
        warp.setBanned(false);
        Chat.admin(sender, "&aWarp &e" + warp.getKey() + " &ais no longer banned.");
    }

    @Subcommand("bancreate")
    @CommandPermission("%admin.ban.create")
    @Syntax("&e<player to ban>")
    @Description("Ban a player from creating any warps")
    @CommandCompletion("@players")
    public static void banWarpOwner(CommandSender sender, OfflinePlayer player) {
        PluginData.BANNED_WARP_OWNERS.get().add(player.getUniqueId());
        Chat.admin(sender, "&aPlayer &e" + player.getName() + " &acan no longer &ccreate &aany warps.");
    }

    @Subcommand("unbancreate")
    @CommandPermission("%admin.unban.create")
    @Syntax("&e<player to unban>")
    @Description("Unban a player from creating any warps")
    @CommandCompletion("@players")
    public static void unbanWarpOwner(CommandSender sender, OfflinePlayer player) {
        PluginData.BANNED_WARP_OWNERS.get().remove(player.getUniqueId());
        Chat.admin(sender, "&aPlayer &e" + player.getName() + " &acan now create warps.");
    }

    @Subcommand("unbancreateall")
    @CommandPermission("%admin.unban.create.all")
    @Syntax("")
    @Description("Unban all player banned from creating warps")
    @CommandCompletion("@players")
    public static void unbanAllWarpOwners(CommandSender sender) {
        PluginData.BANNED_WARP_OWNERS.get().clear();
        Chat.admin(sender, "&aSuccessfully unbanned all players banned from creating warps");
    }

    @Subcommand("adminonly")
    @CommandPermission("%admin.setadminonly")
    @Syntax("&e<true | false | toggle>")
    @Description("Sets whether warp creation is limited to admins only or not")
    @CommandCompletion("@booleans")
    public static void setAdminOnly(CommandSender sender, @Values("toggle|true|false") @Default("toggle") String value) {
        if (value.equals("toggle")) {
            PluginData.ADMIN_ONLY.set(!PluginData.ADMIN_ONLY.get());
            if (PluginData.ADMIN_ONLY.get()) {
                Chat.admin(sender, "&aWarps are now &dadmin-only&a.");
                return;
            }
            Chat.admin(sender, "&aWarps are no longer &dadmin-only&a.");
            return;
        }
        boolean v = Boolean.parseBoolean(value);
        PluginData.ADMIN_ONLY.set(v);
        if (v) {
            Chat.admin(sender, "&aWarps are now &dadmin-only&a.");
            return;
        }
        Chat.admin(sender, "&aWarps are no longer &dadmin-only&a.");
    }

    @Subcommand("warpslimit|limit|createlimit")
    @CommandPermission("%admin.setlimit")
    @Syntax("&e<new warps limit>")
    @Description("Set the amount of warps a player is allowed to have")
    @CommandCompletion("@range:1-10")
    public static void setWarpLimit(CommandSender sender, int limit) {
        PluginData.WARPS_LIMIT.set(limit);
        Chat.admin(sender, "&aWarps limit has been set to &e" + limit + "&a.");
    }

    private static PotionEffect parseEffect(String text) {
        String[] data = text.split(":");
        PotionEffectType type = PotionEffectType.getByName(data[0]);
        if (type == null)
            return null;
        return new PotionEffect(type, Integer.parseInt(data[1]), Integer.parseInt(data[2]));
    }

}