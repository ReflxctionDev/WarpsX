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
import io.github.reflxction.warps.WarpsX;
import io.github.reflxction.warps.messages.Chat;
import io.github.reflxction.warps.messages.MessageKey;
import io.github.reflxction.warps.util.chat.ChatComponent;
import io.github.reflxction.warps.util.chat.ChatEvents.ClickEvent;
import io.github.reflxction.warps.util.chat.ChatEvents.HoverEvent;
import io.github.reflxction.warps.util.chat.ComponentJSON;
import io.github.reflxction.warps.util.chat.PageFooter;
import io.github.reflxction.warps.util.compatibility.Commands;
import io.github.reflxction.warps.util.game.ListPaginator;
import io.github.reflxction.warps.util.game.ListPaginator.Footer;
import io.github.reflxction.warps.util.game.ListPaginator.Header;
import io.github.reflxction.warps.util.game.ListPaginator.MessageConverter;
import io.github.reflxction.warps.util.game.ListPaginator.MessagePlatform;
import io.github.reflxction.warps.util.game.delay.ExclusionManager;
import io.github.reflxction.warps.warp.PlayerWarp;
import io.github.reflxction.warps.warp.WarpController;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;

import java.util.Map;
import java.util.Set;

@CommandAlias("warps")
public class WarpsCommand extends BaseCommand {

    private static final Permission PUBLIC = createDefaultPermission("warpsx.public");
    private static final Permission PRIVATE = createDefaultPermission("warpsx.private");
    private static final Permission CREATE_WARP = createDefaultPermission("warpsx.create");
    private static final Permission GREETING = createDefaultPermission("warpsx.greeting");
    private static final Permission INVITE = createDefaultPermission("warpsx.invite");
    private static final Permission UNINVITE = createDefaultPermission("warpsx.uninvite");
    private static final Permission DELETE_WARP = createDefaultPermission("warpsx.deletewarp");
    private static final Permission CHANGE_LOCATION = createDefaultPermission("warpsx.changelocation");
    private static final Permission SOUND = createDefaultPermission("warpsx.cue");
    private static final Permission UNINVITE_ALL = createDefaultPermission("warpsx.uninviteall");
    private static final Permission LIST_WARPS = createDefaultPermission("warpsx.listwarps");
    private static final Permission LIST_INVITED = createDefaultPermission("warpsx.listinvited");
    private static final Permission COMPASS = createDefaultPermission("warpsx.compass");
    private static final Permission SET_DELAY = createDefaultPermission("warpsx.delay");
    private static final Permission WARP_INFO = createDefaultPermission("warpsx.info");
    private static final Permission BAN_PLAYER = createDefaultPermission("warpsx.ban");
    private static final Permission UNBAN_PLAYER = createDefaultPermission("warpsx.unban");
    private static final Permission UNBAN_ALL = createDefaultPermission("warpsx.unban.all");
    private static final Permission LIST_BANS = createDefaultPermission("warpsx.listbans");
    private static final Permission EXLUCSION = createDefaultPermission("warpsx.exclusion");

    private static final ComponentJSON JSON = new ComponentJSON();

    private static final MessagePlatform<ChatComponent> PLATFORM = (sender, entry) -> JSON.clear().append(entry).send(sender).clear();

    private static final MessageConverter<HelpEntry, ChatComponent> CONVERTER = (entry) -> {
        String message = "&b/" + entry.getCommand() + " " + entry.getParameterSyntax() + " &7- &a" + entry.getDescription().replace(" ", " &a");
        return new ChatComponent().setText(message)
                .setClickAction(ClickEvent.SUGGEST_COMMAND, "/" + entry.getCommand() + " ")
                .setHoverAction(HoverEvent.SHOW_TEXT, "Click to add to your chat box");
    };

    private static final Header HEADER = (sender, pageIndex, pageCount) -> Chat.unprefixed(sender, "&d&m------&r&e Page &a(" + (pageIndex == pageCount ? "&c" : "&7") + pageIndex + " &9/ &c" + (pageCount) + "&a) &d&m------");
    private static final Footer FOOTER = new PageFooter("warps", JSON);

    private static final ListPaginator<HelpEntry, ChatComponent> PAGINATOR = new ListPaginator<>(7, PLATFORM, CONVERTER)
            .setHeader(HEADER).setFooter(FOOTER).ifPageIsInvalid((sender, page) -> Chat.admin(sender, "&cInvalid help page: &e" + page));

    private WarpsX plugin;

    public WarpsCommand(WarpsX plugin) {
        this.plugin = plugin;
    }

    @Subcommand("help|?|-help|-h|-?")
    @CommandCompletion("@range:1-3")
    @Syntax("&e<page number>")
    @Default @CatchUnknown
    @Description("Display help page")
    public void onHelp(CommandSender sender, @Optional @Default("1") int page) {
        if (page == 0) page = 1;
        CommandHelp commandHelp = getCommandHelp();
        PAGINATOR.sendPage(commandHelp.getHelpEntries(), sender, page);
    }

    @Syntax("&e<warp key>") @Description("Set warp to be public")
    @Subcommand("public") @CommandCompletion("@playerwarps")
    public static void setPublic(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, PUBLIC)) {
            Chat.permission(sender);
            return;
        }
        warp.setPublic(true);
        MessageKey.WARP_PUBLIC.send(sender, warp.getLocation(), warp, null);
    }

    @Syntax("&e<warp key>") @Description("Set warp to be private")
    @Subcommand("private") @CommandCompletion("@playerwarps")
    public static void setPrivate(CommandSender e, PlayerWarp warp) {
        if (!Commands.permission(e, PRIVATE)) {
            Chat.permission(e);
            return;
        }
        warp.setPublic(false);
        MessageKey.WARP_PRIVATE.send(e, warp.getLocation(), warp, null);
    }

    @CommandAlias("setwarp|createwarp|addwarp")
    @Syntax("&e<warp key>") @Description("Create a warp point")
    @Conditions("player|claim") @Subcommand("create")
    public void createWarp(CommandSender e, String key) {
        Player player = Commands.safe(e);
        if (!Commands.permission(e, CREATE_WARP)) {
            Chat.permission(e);
            return;
        }
        if (WarpsX.getPluginData().getBannedWarpOwners().contains(player.getUniqueId())) {
            MessageKey.CREATE_BANNED.send(player, null, null, player);
            return;
        }
        if (WarpsX.getPluginData().isAdminOnly()) {
            if (!player.hasPermission("warpsx.admin.create")) {
                Chat.permission(e);
                return;
            }
        }
        if (!player.hasPermission("warpsx.bypass.creationlimit") && WarpController.getWarps(player).size() >= WarpsX.getPluginData().getWarpsLimit()) {
            MessageKey.WARP_LIMIT.send(player, null, null, player);
            return;
        }
        if (plugin.getWarpKeys().contains(key)) {
            MessageKey.WARP_ALREADY_EXISTS.send(player, null, null, player, "{warp_key}", key);
            return;
        }
        if (key.contains(" ")) {
            Chat.admin(player, "&cA warp key must not contain spaces!");
            return;
        }
        PlayerWarp warp = new PlayerWarp.Builder().key(key).location(player.getLocation()).owner(player).build();
        WarpController.createWarp(warp);
        MessageKey.WARP_CREATED.send(player, warp.getLocation(), warp, null);
    }

    @Syntax("&e<warp key> &e<greeting message>") @Description("Set the greeting message of a warp")
    @Subcommand("greet|greeting") @CommandCompletion("@playerwarps @nothing")
    public static void greet(CommandSender sender, PlayerWarp warp, String greeting) {
        if (!Commands.permission(sender, GREETING)) {
            Chat.permission(sender);
            return;
        }
        warp.setGreetingMessage(greeting);
        MessageKey.GREETING_UPDATED.send(sender, null, warp, null);
    }

    @Syntax("&e<warp key> &e<player to invite>") @Description("Invite a player to your warp")
    @Subcommand("invite") @CommandCompletion("@playerwarps @players")
    @Conditions("player")
    public static void invite(CommandSender e, PlayerWarp warp, OfflinePlayer invited) {
        if (!Commands.permission(e, INVITE)) {
            Chat.permission(e);
            return;
        }
        Player player = Commands.safe(e);
        if (player.getUniqueId().equals(invited.getUniqueId())) {
            MessageKey.CANNOT_INVITE_SELF.send(e, warp.getLocation(), warp, player);
            return;
        }
        if (!warp.canModify(player)) {
            MessageKey.NOT_WARP_OWNER.send(e, warp.getLocation(), warp, player);
            return;
        }
        warp.invite(invited);
        MessageKey.INVITED_PLAYER.send(e, warp.getLocation(), warp, invited);
    }

    @Syntax("&e<warp key> &e<player to uninvite>") @Description("Uninvite a player to your warp")
    @Subcommand("uninvite") @CommandCompletion("@playerwarps @players")
    @Conditions("player")
    public static void uninvite(CommandSender e, PlayerWarp warp, OfflinePlayer target) {
        if (!Commands.permission(e, UNINVITE)) {
            Chat.permission(e);
            return;
        }
        Player player = Commands.safe(e);
        if (player.getUniqueId().equals(target.getUniqueId())) {
            MessageKey.CANNOT_UNINVITE_SELF.send(e, warp.getLocation(), warp, player);
            return;
        }
        if (!warp.canModify(player)) {
            MessageKey.NOT_WARP_OWNER.send(e, warp.getLocation(), warp, player);
            return;
        }
        if (!warp.isInvited(target)) {
            MessageKey.PLAYER_NOT_INVITED.send(e, warp.getLocation(), warp, target);
            return;
        }
        warp.uninvite(target);
        MessageKey.UNINVITED_PLAYER.send(e, warp.getLocation(), warp, target);
    }

    @CommandAlias("delwarp|removewarp")
    @Syntax("&e<warp key>") @Description("Remove a warp")
    @Subcommand("delete|remove") @CommandCompletion("@playerwarps")
    public static void deleteWarp(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, DELETE_WARP)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        WarpController.removeWarp(warp.getKey());
        MessageKey.WARP_DELETED.send(sender, warp.getLocation(), warp, null);
    }

    @Syntax("&e<warp key>") @Description("Change the location of a warp")
    @Subcommand("changeloc|changelocation|updateloc|loc") @CommandCompletion("@playerwarps")
    @Conditions("player")
    public static void changeLocation(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, CHANGE_LOCATION)) {
            Chat.permission(sender);
            return;
        }
        Player player = Commands.safe(sender);
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, player);
            return;
        }
        Location location = player.getLocation();
        warp.setLocation(location);
        MessageKey.LOCATION_UPDATED.send(sender, location, warp, null);
    }

    @Syntax("&e<warp key>") @Description("Remove the sound of the warp")
    @Subcommand("removecue|removesound") @CommandCompletion("@playerwarps")
    public static void removeSound(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, SOUND)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        warp.setSound(null);
        MessageKey.SOUND_REMOVED.send(sender, warp.getLocation(), warp, null);
    }

    @Syntax("&e<warp key> <sound>") @Description("Set the sound/cue played when the warp is used")
    @Subcommand("cue|sound") @CommandCompletion("@playerwarps @sounds")
    public static void setSound(CommandSender sender, PlayerWarp warp, Sound sound) {
        if (!Commands.permission(sender, SOUND)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        warp.setSound(sound);
        MessageKey.SOUND_ADDED.send(sender, warp.getLocation(), warp, null, "{sound}", sound.name().toLowerCase());
    }

    @Syntax("&e<warp key>") @Description("Uninvite all players from your home")
    @CommandCompletion("@playerwarps") @Subcommand("uninvall|uninviteall")
    public static void uninviteAll(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, UNINVITE_ALL)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        warp.getInvited().clear();
        MessageKey.UNINVITED_ALL.send(sender, warp.getLocation(), warp, null);
        if (warp.isPublic())
            MessageKey.PUBLIC_NOTE.send(sender, warp.getLocation(), warp, null);
    }

    @Syntax("") @Description("List all your warps")
    @CommandCompletion("@nothing") @Subcommand("list|listwarps") @Conditions("player")
    public static void listWarps(CommandSender sender) {
        if (!Commands.permission(sender, LIST_WARPS)) {
            Chat.permission(sender);
            return;
        }
        Player player = Commands.safe(sender);
        Map<String, PlayerWarp> warps = WarpController.getWarps(player);
        if (warps.isEmpty()) {
            MessageKey.NO_WARPS.send(sender, null, null, player);
            return;
        }
        warps.keySet().forEach(key -> Chat.unprefixed(sender, "&7- &e" + key));
    }

    @Syntax("&e<warp key>") @Description("List players who are invited to your warp")
    @CommandCompletion("@playerwarps") @Subcommand("listinv|listinvited")
    public static void listInvited(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, LIST_INVITED)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        warp.getInvited().forEach(inv -> Chat.unprefixed(sender, "&7- &e" + inv.getName()));
    }

    @Subcommand("compass")
    @Syntax("&e<warp key>")
    @Description("Point your compass to the specified warp")
    @Conditions("player")
    @CommandCompletion("@playerwarps")
    public static void pointCompass(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, COMPASS)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        Player player = Commands.safe(sender);
        player.setCompassTarget(warp.getLocation());
        Chat.admin(sender, "&aYour compass now points to warp &e" + warp.getKey() + "&a.");
    }

    @Subcommand("delay")
    @Syntax("&e<warp key> <delay>")
    @Description("Set the delay of a warp")
    @CommandCompletion("@playerwarps @range:1-20")
    public static void delay(CommandSender sender, PlayerWarp warp, @Default("10") int delay) {
        if (!Commands.permission(sender, SET_DELAY)) {
            Chat.permission(sender);
            return;
        }
        warp.setDelay(delay);
        MessageKey.DELAY_SET.send(sender, warp.getLocation(), warp, null);
    }

    @CommandAlias("winfo|warpinfo")
    @Subcommand("info|warpinfo")
    @Syntax("&e<warp key>")
    @Description("Get all information about a specific warp")
    @CommandCompletion("@playerwarps")
    public void getWarpInfo(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, WARP_INFO)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        Chat.unprefixed(sender, "&7-- &dWarp info for &e" + warp.getKey() + "&d: ");
        Chat.unprefixed(sender, "&eKey: &d" + warp.getKey());
        Chat.unprefixed(sender, "&eOwner: &d" + warp.getOwner().getName() + " &7(" + warp.getOwner().getUniqueId() + ")");
        Chat.unprefixed(sender, "&eDelay: &d" + warp.getDelay() + "s");
        Chat.unprefixed(sender, "&ePublic: &d" + warp.isPublic());
        Location l = warp.getLocation();
        Chat.unprefixed(sender, "&eLocation: &bX: &d" + l.getBlockX() + "&b, Y: &d" + l.getBlockY() + "&b, Z: &d" + l.getBlockZ());
        Chat.unprefixed(sender, "&eGreeting message: &d" + warp.getGreetingMessage());
        Chat.unprefixed(sender, "&eCue sound: &d" + (warp.getSound() == null ? "&cNone" : warp.getSound().name().toLowerCase()));
        Set<PotionEffect> e = warp.getPotionEffects();
        Chat.unprefixed(sender, "&ePotion effects: " + (e.isEmpty() ? "&cNone" : ""));
        e.forEach(effect -> Chat.unprefixed(sender, "&7- &eT: &d" + effect.getType().getName().toLowerCase() + " &7/ &eD: &d" + effect.getDuration() + " &7/ &eA: &d" + effect.getAmplifier()));
    }

    @Subcommand("ban|revoke")
    @Syntax("&e<warp key> <player to ban>")
    @Description("Ban a player from using this warp")
    @CommandCompletion("@playerwarps @players")
    public void banPlayer(CommandSender sender, PlayerWarp warp, OfflinePlayer banned) {
        if (!Commands.permission(sender, BAN_PLAYER)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        if (warp.isBanned(banned)) {
            MessageKey.PLAYER_ALREADY_BANNED.send(sender, warp.getLocation(), warp, banned);
            return;
        }
        warp.banPlayer(banned);
        MessageKey.PLAYER_BANNED.send(sender, warp.getLocation(), warp, banned);
    }

    @Subcommand("unban|pardon")
    @Syntax("&e<warp key> <player to unban>")
    @Description("Unbans a player and allows them to use the warp again")
    @CommandCompletion("@playerwarps @players")
    public void unbanPlayer(CommandSender sender, PlayerWarp warp, OfflinePlayer unban) {
        if (!Commands.permission(sender, UNBAN_PLAYER)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }

        if (!warp.isBanned(unban)) {
            MessageKey.PLAYER_NOT_BANNED.send(sender, warp.getLocation(), warp, unban);
            return;
        }
        warp.unbanPlayer(unban);
        MessageKey.PLAYER_UNBANNED.send(sender, warp.getLocation(), warp, unban);
    }

    @Subcommand("banlist|bans|listbans")
    @Syntax("&e<warp key>")
    @Description("List all banned players in a specific warp")
    @CommandCompletion("@playerwarps")
    public void listBans(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, LIST_BANS)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        Chat.unprefixed(sender, "&eBanned players: " + (warp.getBannedPlayers().size() == 0 ? "&cNone!" : ""));
        warp.getBannedPlayers().forEach(inv -> Chat.unprefixed(sender, "&7- &e" + inv.getName()));
    }

    @Subcommand("unbanall|pardonall")
    @Syntax("&e<warp key>")
    @Description("Unban all banned players from a specific warp")
    @CommandCompletion("@playerwarps")
    public void unbanAll(CommandSender sender, PlayerWarp warp) {
        if (!Commands.permission(sender, UNBAN_ALL)) {
            Chat.permission(sender);
            return;
        }
        if (!warp.canModify(sender)) {
            MessageKey.NOT_WARP_OWNER.send(sender, warp.getLocation(), warp, null);
            return;
        }
        warp.getBannedPlayers().clear();
        MessageKey.UNBANNED_ALL.send(sender, warp.getLocation(), warp, null);
    }

    @Subcommand("exclusion")
    @Syntax("&e<warp key> <open duration>")
    @Description("Sets the warp open duration")
    @CommandCompletion("@playerwarps @range:1-10")
    public void setExclusion(CommandSender sender, PlayerWarp warp, int duration) {
        if (!Commands.permission(sender, EXLUCSION)) {
            Chat.permission(sender);
            return;
        }
        warp.setExclusion(duration);
        ExclusionManager.startExclusion(warp);
        Chat.admin(sender, "&aWarp &e" + warp.getKey() + " &awill be public for &e" + duration + " &asecond" + (duration == 1 ? "" : "s") + ".");
    }

    @Private
    @Description("Get the plugin version")
    @Subcommand("v|version")
    public void getVersion(CommandSender sender) {
        Chat.admin(sender, "&aWarpsX v&d" + plugin.getDescription().getVersion());
    }

    private static Permission createDefaultPermission(String permission) {
        return new Permission(permission, PermissionDefault.TRUE);
    }

}