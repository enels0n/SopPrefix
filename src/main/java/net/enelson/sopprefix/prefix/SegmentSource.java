package net.enelson.sopprefix.prefix;

public enum SegmentSource {
    EDITABLE,
    PLACEHOLDER,
    FIXED,
    PLAYER_NAME;

    public static SegmentSource fromConfig(String input, boolean editable) {
        if (editable) {
            return EDITABLE;
        }
        if (input == null) {
            return PLACEHOLDER;
        }

        String normalized = input.trim().toLowerCase();
        if (normalized.equals("fixed")) {
            return FIXED;
        }
        if (normalized.equals("player-name")) {
            return PLAYER_NAME;
        }
        return PLACEHOLDER;
    }
}
