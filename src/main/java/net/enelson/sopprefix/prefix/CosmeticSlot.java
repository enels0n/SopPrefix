package net.enelson.sopprefix.prefix;

public enum CosmeticSlot {
    PREFIX("prefix"),
    SUFFIX_1("suffix1"),
    SUFFIX_2("suffix2");

    private final String id;

    CosmeticSlot(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    public static CosmeticSlot fromInput(String input) {
        if (input == null) {
            return null;
        }

        String normalized = input.toLowerCase();
        if (normalized.equals("1") || normalized.equals("first") || normalized.equals("suffix1")) {
            return SUFFIX_1;
        }
        if (normalized.equals("2") || normalized.equals("second") || normalized.equals("suffix2")) {
            return SUFFIX_2;
        }
        if (normalized.equals("prefix")) {
            return PREFIX;
        }
        return null;
    }
}
