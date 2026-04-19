package net.enelson.sopprefix.command;

import net.enelson.sopprefix.gui.SuffixMenuService;
import net.enelson.sopprefix.prefix.FormatDefinition;
import net.enelson.sopprefix.prefix.PrefixDefinition;
import net.enelson.sopprefix.prefix.PrefixManager;
import net.enelson.sopprefix.prefix.SegmentSide;
import net.enelson.sopprefix.prefix.TagSegment;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SuffixCommand implements CommandExecutor, TabCompleter {

    private final PrefixManager prefixManager;
    private final SuffixMenuService menuService;

    public SuffixCommand(PrefixManager prefixManager, SuffixMenuService menuService) {
        this.prefixManager = prefixManager;
        this.menuService = menuService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(this.prefixManager.getMessage("player-only"));
                return true;
            }
            this.menuService.openMainMenu((Player) sender);
            return true;
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("reload")) {
            if (!sender.hasPermission("sopprefix.admin")) {
                sender.sendMessage(this.prefixManager.getMessage("no-permission"));
                return true;
            }
            this.prefixManager.reload();
            sender.sendMessage(this.prefixManager.getMessage("reload"));
            return true;
        }

        if (isAdminAction(sub)) {
            return handleAdmin(sender, sub, args);
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(this.prefixManager.getMessage("player-only"));
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 2) {
            this.menuService.openMainMenu(player);
            return true;
        }

        String segmentId = args[1];
        TagSegment segment = findSegment(segmentId);
        if (segment == null) {
            sender.sendMessage(this.prefixManager.getMessage("segment-not-found"));
            return true;
        }

        if (sub.equals("clear")) {
            this.prefixManager.clearActiveText(player, segment.getId());
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-cleared"), segmentMap(segment.getName(), "")));
            return true;
        }
        if (sub.equals("clearformat")) {
            this.prefixManager.clearActiveFormat(player, segment.getId());
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-cleared"), segmentMap(segment.getName(), "")));
            return true;
        }
        if (sub.equals("random")) {
            PrefixDefinition definition = this.prefixManager.setRandomText(player, segment.getId());
            if (definition == null) {
                player.sendMessage(this.prefixManager.getMessage("no-texts"));
                return true;
            }
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-set"), segmentMap(segment.getName(), definition.getValue())));
            return true;
        }
        if (sub.equals("randomformat")) {
            FormatDefinition definition = this.prefixManager.setRandomFormat(player, segment.getId());
            if (definition == null) {
                player.sendMessage(this.prefixManager.getMessage("no-formats"));
                return true;
            }
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-set"), segmentMap(segment.getName(), definition.getDisplayName())));
            return true;
        }
        if (args.length < 3) {
            return false;
        }
        if (sub.equals("set")) {
            PrefixDefinition definition = this.prefixManager.getDefinition(segment.getId(), args[2]);
            if (definition == null) {
                player.sendMessage(this.prefixManager.getMessage("text-not-found"));
                return true;
            }
            if (!this.prefixManager.setActiveText(player, segment.getId(), definition.getId())) {
                player.sendMessage(this.prefixManager.getMessage("text-not-available"));
                return true;
            }
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-set"), segmentMap(segment.getName(), definition.getValue())));
            return true;
        }
        if (sub.equals("format")) {
            FormatDefinition definition = this.prefixManager.getFormatDefinition(segment.getId(), args[2]);
            if (definition == null) {
                player.sendMessage(this.prefixManager.getMessage("format-not-found"));
                return true;
            }
            if (!this.prefixManager.setActiveFormat(player, segment.getId(), definition.getId())) {
                player.sendMessage(this.prefixManager.getMessage("format-not-available"));
                return true;
            }
            player.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-set"), segmentMap(segment.getName(), definition.getDisplayName())));
            return true;
        }

        this.menuService.openMainMenu(player);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return filter(Arrays.asList("set", "format", "clear", "clearformat", "random", "randomformat", "reload", "give", "take", "giveformat", "takeformat"), args[0]);
        }
        if (args.length == 2 && !isAdminAction(args[0])) {
            return filter(segmentIds(), args[1]);
        }
        if (args.length == 2 && isAdminAction(args[0])) {
            return filter(onlinePlayers(), args[1]);
        }
        if (args.length == 3 && isAdminAction(args[0])) {
            return filter(segmentIds(), args[2]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            TagSegment segment = findSegment(args[1]);
            return segment == null ? Collections.<String>emptyList() : filter(new ArrayList<String>(this.prefixManager.getDefinitionIds(segment.getId())), args[2]);
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("format")) {
            TagSegment segment = findSegment(args[1]);
            return segment == null ? Collections.<String>emptyList() : filter(new ArrayList<String>(this.prefixManager.getFormatIds(segment.getId())), args[2]);
        }
        if (args.length == 4 && (args[0].equalsIgnoreCase("give") || args[0].equalsIgnoreCase("take"))) {
            TagSegment segment = findSegment(args[2]);
            return segment == null ? Collections.<String>emptyList() : filter(new ArrayList<String>(this.prefixManager.getDefinitionIds(segment.getId())), args[3]);
        }
        if (args.length == 4 && (args[0].equalsIgnoreCase("giveformat") || args[0].equalsIgnoreCase("takeformat"))) {
            TagSegment segment = findSegment(args[2]);
            return segment == null ? Collections.<String>emptyList() : filter(new ArrayList<String>(this.prefixManager.getFormatIds(segment.getId())), args[3]);
        }
        return Collections.emptyList();
    }

    private boolean handleAdmin(CommandSender sender, String sub, String[] args) {
        if (!sender.hasPermission("sopprefix.admin")) {
            sender.sendMessage(this.prefixManager.getMessage("no-permission"));
            return true;
        }
        if (args.length < 4) {
            return false;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        TagSegment segment = findSegment(args[2]);
        if (segment == null) {
            sender.sendMessage(this.prefixManager.getMessage("segment-not-found"));
            return true;
        }
        String playerName = player.getName() == null ? args[1] : player.getName();

        if (sub.equals("give") || sub.equals("take")) {
            PrefixDefinition definition = this.prefixManager.getDefinition(segment.getId(), args[3]);
            if (definition == null) {
                sender.sendMessage(this.prefixManager.getMessage("text-not-found"));
                return true;
            }
            if (sub.equals("give")) {
                this.prefixManager.grantDefinition(player, segment.getId(), definition.getId());
                sender.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-given"), adminMap(playerName, segment.getName(), definition.getValue())));
            } else {
                this.prefixManager.takeDefinition(player, segment.getId(), definition.getId());
                sender.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("text-taken"), adminMap(playerName, segment.getName(), definition.getValue())));
            }
            return true;
        }

        FormatDefinition definition = this.prefixManager.getFormatDefinition(segment.getId(), args[3]);
        if (definition == null) {
            sender.sendMessage(this.prefixManager.getMessage("format-not-found"));
            return true;
        }
        if (sub.equals("giveformat")) {
            this.prefixManager.grantFormat(player, segment.getId(), definition.getId());
            sender.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-given"), adminMap(playerName, segment.getName(), definition.getDisplayName())));
        } else {
            this.prefixManager.takeFormat(player, segment.getId(), definition.getId());
            sender.sendMessage(this.prefixManager.format(this.prefixManager.getMessage("format-taken"), adminMap(playerName, segment.getName(), definition.getDisplayName())));
        }
        return true;
    }

    private TagSegment findSegment(String input) {
        TagSegment segment = this.prefixManager.getSegment(input);
        return segment != null && segment.isEditable() && segment.getSide() == SegmentSide.SUFFIX ? segment : null;
    }

    private List<String> segmentIds() {
        List<String> ids = new ArrayList<String>();
        for (TagSegment segment : this.prefixManager.getEditableSegments(SegmentSide.SUFFIX)) {
            ids.add(segment.getId());
        }
        return ids;
    }

    private boolean isAdminAction(String input) {
        String lower = input.toLowerCase();
        return lower.equals("give") || lower.equals("take") || lower.equals("giveformat") || lower.equals("takeformat");
    }

    private List<String> onlinePlayers() {
        List<String> players = new ArrayList<String>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getName());
        }
        return players;
    }

    private List<String> filter(List<String> values, String input) {
        List<String> result = new ArrayList<String>();
        String lowerInput = input.toLowerCase();
        for (String value : values) {
            if (value.toLowerCase().startsWith(lowerInput)) {
                result.add(value);
            }
        }
        return result;
    }

    private Map<String, String> segmentMap(String segmentName, String value) {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("%segment%", segmentName);
        replacements.put("%value%", value == null ? "" : value);
        return replacements;
    }

    private Map<String, String> adminMap(String playerName, String segmentName, String value) {
        Map<String, String> replacements = segmentMap(segmentName, value);
        replacements.put("%player%", playerName == null ? "" : playerName);
        return replacements;
    }
}
