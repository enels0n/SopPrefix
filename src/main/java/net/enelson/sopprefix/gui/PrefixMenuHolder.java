package net.enelson.sopprefix.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class PrefixMenuHolder implements InventoryHolder {

    private final MenuType type;
    private final String segmentId;
    private final String categoryId;
    private final int page;
    private final boolean availableOnly;

    public PrefixMenuHolder(MenuType type, String segmentId, String categoryId, int page, boolean availableOnly) {
        this.type = type;
        this.segmentId = segmentId;
        this.categoryId = categoryId;
        this.page = page;
        this.availableOnly = availableOnly;
    }

    public MenuType getType() {
        return this.type;
    }

    public String getSegmentId() {
        return this.segmentId;
    }

    public String getCategoryId() {
        return this.categoryId;
    }

    public int getPage() {
        return this.page;
    }

    public boolean isAvailableOnly() {
        return this.availableOnly;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }
}
