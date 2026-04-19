package net.enelson.sopprefix.prefix;

import java.util.List;

public final class PrefixCategory {

    private final String id;
    private final String name;
    private final String materialSpec;
    private final int slot;
    private final List<String> lore;

    public PrefixCategory(String id, String name, String materialSpec, int slot, List<String> lore) {
        this.id = id;
        this.name = name;
        this.materialSpec = materialSpec;
        this.slot = slot;
        this.lore = lore;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getMaterialSpec() {
        return this.materialSpec;
    }

    public int getSlot() {
        return this.slot;
    }

    public List<String> getLore() {
        return this.lore;
    }
}
