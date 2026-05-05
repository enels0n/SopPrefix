package net.enelson.sopprefix.util;

import net.enelson.sopli.lib.SopLib;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Text {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern MINI_MESSAGE_GRADIENT_PATTERN = Pattern.compile(
            "<(gradient|transition):([^>]+)>",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern MINI_MESSAGE_BLOCK_PATTERN = Pattern.compile(
            "<[a-zA-Z0-9_#!:?./-][^>]*>.*?</[a-zA-Z0-9_:-]+>",
            Pattern.CASE_INSENSITIVE
    );
    private static final char SECTION_CHAR = '\u00A7';

    private static volatile boolean miniMessageChecked;
    private static volatile Object miniMessageInstance;
    private static volatile Method deserializeMethod;
    private static volatile Object legacySerializerInstance;
    private static volatile Method serializeMethod;

    private Text() {
    }

    public static String color(String input) {
        if (input == null) {
            return "";
        }

        SopLib sopLib = SopLib.getInstance();
        if (sopLib != null && sopLib.getTextUtils() != null) {
            return sopLib.getTextUtils().color(input);
        }

        String normalized = translateLegacyColors(resolveMiniMessageBlocks(normalizeMiniMessageTags(input)));
        return normalized == null ? "" : normalized;
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

    private static String resolveMiniMessageBlocks(String input) {
        if (input.indexOf('<') < 0 || input.indexOf('>') < 0) {
            return input;
        }

        Matcher matcher = MINI_MESSAGE_BLOCK_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            String resolved = miniMessageToLegacy(matcher.group());
            matcher.appendReplacement(result, Matcher.quoteReplacement(resolved));
            replaced = true;
        }
        if (!replaced) {
            return input;
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String normalizeMiniMessageTags(String input) {
        if (input == null || input.indexOf('<') < 0 || input.indexOf('>') < 0) {
            return input;
        }

        Matcher matcher = MINI_MESSAGE_GRADIENT_PATTERN.matcher(input);
        StringBuffer result = new StringBuffer();
        boolean replaced = false;
        while (matcher.find()) {
            String type = matcher.group(1);
            String arguments = matcher.group(2).replaceAll("(?i)(#[a-f0-9]{6})(?=#)", "$1:");
            matcher.appendReplacement(result, Matcher.quoteReplacement("<" + type + ":" + arguments + ">"));
            replaced = true;
        }
        if (!replaced) {
            return input;
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String miniMessageToLegacy(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        if (!ensureMiniMessageSupport()) {
            return input;
        }

        try {
            Class<?> tagResolverClass = Class.forName("net.kyori.adventure.text.minimessage.tag.resolver.TagResolver");
            Object emptyResolvers = java.lang.reflect.Array.newInstance(tagResolverClass, 0);
            Object component = deserializeMethod.invoke(miniMessageInstance, input.replace(SECTION_CHAR, '&'), emptyResolvers);
            Object serialized = serializeMethod.invoke(legacySerializerInstance, component);
            return serialized instanceof String ? (String) serialized : input;
        } catch (Throwable ignored) {
            return input;
        }
    }

    private static String translateLegacyColors(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(buffer, ChatColor.of("#" + matcher.group(1)).toString());
        }
        matcher.appendTail(buffer);
        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    private static synchronized boolean ensureMiniMessageSupport() {
        if (miniMessageChecked) {
            return miniMessageInstance != null && deserializeMethod != null
                    && legacySerializerInstance != null && serializeMethod != null;
        }

        miniMessageChecked = true;
        try {
            Class<?> componentClass = Class.forName("net.kyori.adventure.text.Component");
            Class<?> miniMessageClass = Class.forName("net.kyori.adventure.text.minimessage.MiniMessage");
            Object mini = miniMessageClass.getMethod("miniMessage").invoke(null);
            Class<?> tagResolverArrayClass = Class.forName("[Lnet.kyori.adventure.text.minimessage.tag.resolver.TagResolver;");
            Method deserialize = miniMessageClass.getMethod("deserialize", String.class, tagResolverArrayClass);

            Class<?> serializerClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");
            Object builder = serializerClass.getMethod("builder").invoke(null);
            Class<?> builderClass = builder.getClass();
            builder = builderClass.getMethod("character", char.class).invoke(builder, SECTION_CHAR);
            builder = builderClass.getMethod("hexColors").invoke(builder);
            builder = builderClass.getMethod("useUnusualXRepeatedCharacterHexFormat").invoke(builder);
            Object serializer = builderClass.getMethod("build").invoke(builder);
            Method serialize = serializerClass.getMethod("serialize", componentClass);

            miniMessageInstance = mini;
            deserializeMethod = deserialize;
            legacySerializerInstance = serializer;
            serializeMethod = serialize;
        } catch (Throwable ignored) {
            miniMessageInstance = null;
            deserializeMethod = null;
            legacySerializerInstance = null;
            serializeMethod = null;
        }

        return miniMessageInstance != null && deserializeMethod != null
                && legacySerializerInstance != null && serializeMethod != null;
    }
}
