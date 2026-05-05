package net.enelson.sopprefix.prefix;

import java.util.List;

public final class PrefixDefinition {

    private final String id;
    private final String segmentId;
    private final String displayName;
    private final String value;
    private final String categoryId;
    private final String materialSpec;
    private final String permission;
    private final List<String> lore;
    private final List<String> frames;
    private final int intervalTicks;

    public PrefixDefinition(String id, String segmentId, String displayName, String value, String categoryId, String materialSpec, String permission, List<String> lore, List<String> frames, int intervalTicks) {
        this.id = id;
        this.segmentId = segmentId;
        this.displayName = displayName;
        this.value = value;
        this.categoryId = categoryId;
        this.materialSpec = materialSpec;
        this.permission = permission;
        this.lore = lore;
        this.frames = frames;
        this.intervalTicks = intervalTicks;
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

    public String getValue() {
        return this.value;
    }

    public String getCategoryId() {
        return this.categoryId;
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

    public List<String> getFrames() {
        return this.frames;
    }

    public int getIntervalTicks() {
        return this.intervalTicks;
    }

    public boolean isAnimated() {
        return this.frames != null && !this.frames.isEmpty() && this.intervalTicks > 0;
    }

    public String resolveValue(long tick) {
        if (!isAnimated()) {
            return this.value;
        }
        int index = (int) ((tick / this.intervalTicks) % this.frames.size());
        return this.frames.get(index);
    }
}
