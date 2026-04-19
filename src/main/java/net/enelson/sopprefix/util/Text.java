package net.enelson.sopprefix.util;

import net.enelson.sopli.lib.SopLib;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Text {

    private Text() {
    }

    public static String color(String input) {
        if (SopLib.getInstance() != null) {
            return SopLib.getInstance().getTextUtils().color(input);
        }
        return input == null ? "" : input;
    }

    public static List<String> color(List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<String>(lines.size());
        for (String line : lines) {
            result.add(color(line));
        }
        return result;
    }
}
