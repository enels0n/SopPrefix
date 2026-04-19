package net.enelson.sopprefix.prefix;

import java.util.List;

public final class FormatDefinition {

    private final String id;
    private final String segmentId;
    private final String displayName;
    private final String format;
    private final String materialSpec;
    private final String permission;
    private final List<String> lore;

    public FormatDefinition(String id, String segmentId, String displayName, String format, String materialSpec, String permission, List<String> lore) {
        this.id = id;
        this.segmentId = segmentId;
        this.displayName = displayName;
        this.format = format;
        this.materialSpec = materialSpec;
        this.permission = permission;
        this.lore = lore;
    }

    public String getId() {
        return this.id;
    }

    public String getSegmentId() {
        return this.segmentId;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFormat() {
        return this.format;
    }

    public String getMaterialSpec() {
        return this.materialSpec;
    }

    public String getPermission() {
        return this.permission;
    }

    public List<String> getLore() {
        return this.lore;
    }
}
