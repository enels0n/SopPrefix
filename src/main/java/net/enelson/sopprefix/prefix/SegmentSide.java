package net.enelson.sopprefix.prefix;

public enum SegmentSide {
    PREFIX,
    SUFFIX;

    public static SegmentSide fromConfig(String input) {
        if (input == null) {
            return PREFIX;
        }

        String normalized = input.trim().toLowerCase();
        if (normalized.equals("suffix")) {
            return SUFFIX;
        }
        return PREFIX;
    }
}
