package net.enelson.sopprefix.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.enelson.sopprefix.prefix.PrefixManager;
import net.enelson.sopprefix.prefix.SegmentSide;
import org.bukkit.entity.Player;

public final class SopPrefixPlaceholders extends PlaceholderExpansion {

    private final PrefixManager prefixManager;

    public SopPrefixPlaceholders(PrefixManager prefixManager) {
        this.prefixManager = prefixManager;
    }

    @Override
    public String getIdentifier() {
        return "sopprefix";
    }

    @Override
    public String getAuthor() {
        return "enels0n";
    }

    @Override
    public String getVersion() {
        return "0.0.3";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("fullname") || params.equalsIgnoreCase("display_name")) {
            return this.prefixManager.getFullName(player);
        }
        if (params.equalsIgnoreCase("before_name")) {
            return this.prefixManager.getBeforeName(player);
        }
        if (params.equalsIgnoreCase("before_name_with_separator")) {
            return this.prefixManager.getBeforeNameWithSeparator(player);
        }
        if (params.equalsIgnoreCase("after_name")) {
            return this.prefixManager.getAfterName(player);
        }
        if (params.equalsIgnoreCase("after_name_with_separator")) {
            return this.prefixManager.getAfterNameWithSeparator(player);
        }
        if (params.equalsIgnoreCase("preview") || params.equalsIgnoreCase("tag")) {
            return this.prefixManager.getPreview(player);
        }
        if (params.equalsIgnoreCase("prefix")) {
            return this.prefixManager.getFormattedSegment(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.PREFIX, 0));
        }
        if (params.equalsIgnoreCase("prefix_raw")) {
            return this.prefixManager.getRawSegmentValue(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.PREFIX, 0));
        }
        if (params.equalsIgnoreCase("prefix_id")) {
            return this.prefixManager.getActiveId(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.PREFIX, 0));
        }
        if (params.equalsIgnoreCase("prefix_format_id")) {
            return this.prefixManager.getActiveFormatId(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.PREFIX, 0));
        }
        if (params.equalsIgnoreCase("suffix1")) {
            return this.prefixManager.getFormattedSegment(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 0));
        }
        if (params.equalsIgnoreCase("suffix1_raw")) {
            return this.prefixManager.getRawSegmentValue(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 0));
        }
        if (params.equalsIgnoreCase("suffix1_id")) {
            return this.prefixManager.getActiveId(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 0));
        }
        if (params.equalsIgnoreCase("suffix1_format_id")) {
            return this.prefixManager.getActiveFormatId(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 0));
        }
        if (params.equalsIgnoreCase("suffix2")) {
            return this.prefixManager.getFormattedSegment(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 1));
        }
        if (params.equalsIgnoreCase("suffix2_raw")) {
            return this.prefixManager.getRawSegmentValue(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 1));
        }
        if (params.equalsIgnoreCase("suffix2_id")) {
            return this.prefixManager.getActiveId(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 1));
        }
        if (params.equalsIgnoreCase("suffix2_format_id")) {
            return this.prefixManager.getActiveFormatId(player, this.prefixManager.getCompatibilitySegmentId(SegmentSide.SUFFIX, 1));
        }

        if (params.startsWith("segment_")) {
            String segmentId = params.substring("segment_".length());
            return this.prefixManager.getFormattedSegment(player, segmentId);
        }
        if (params.startsWith("segment_raw_")) {
            String segmentId = params.substring("segment_raw_".length());
            return this.prefixManager.getRawSegmentValue(player, segmentId);
        }
        if (params.startsWith("segment_id_")) {
            String segmentId = params.substring("segment_id_".length());
            return this.prefixManager.getActiveId(player, segmentId);
        }
        if (params.startsWith("segment_format_id_")) {
            String segmentId = params.substring("segment_format_id_".length());
            return this.prefixManager.getActiveFormatId(player, segmentId);
        }

        return null;
    }
}
