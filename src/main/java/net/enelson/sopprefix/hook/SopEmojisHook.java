package net.enelson.sopprefix.hook;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SopEmojisHook {

    private static final Pattern TOKEN_PATTERN = Pattern.compile("sopemojis:([A-Za-z0-9_.-]+)", Pattern.CASE_INSENSITIVE);

    private static Method resolveByIdMethod;
    private static Method resolveTextMethod;
    private static Class<?> cachedPluginClass;

    private SopEmojisHook() {
    }

    public static String apply(CommandSender sender, String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("SopEmojis");
        if (plugin == null || !plugin.isEnabled()) {
            return input;
        }

        String result = input;
        if (sender != null) {
            result = replaceIdTokens(plugin, sender, result);
            result = resolveText(plugin, sender, result);
        }
        return result;
    }

    private static String replaceIdTokens(Plugin plugin, CommandSender sender, String input) {
        Matcher matcher = TOKEN_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        boolean changed = false;
        while (matcher.find()) {
            changed = true;
            String replacement = resolveById(plugin, sender, matcher.group(1));
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacement));
        }
        if (!changed) {
            return input;
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private static String resolveById(Plugin plugin, CommandSender sender, String emojiId) {
        try {
            Method method = resolveByIdMethod(plugin);
            if (method == null) {
                return "";
            }
            Object result = method.invoke(plugin, sender, emojiId);
            return result instanceof String ? (String) result : "";
        } catch (Throwable ignored) {
            return "";
        }
    }

    private static String resolveText(Plugin plugin, CommandSender sender, String input) {
        try {
            Method method = resolveTextMethod(plugin);
            if (method == null) {
                return input;
            }
            Object result = method.invoke(plugin, sender, input);
            return result instanceof String ? (String) result : input;
        } catch (Throwable ignored) {
            return input;
        }
    }

    private static Method resolveByIdMethod(Plugin plugin) {
        Class<?> pluginClass = plugin.getClass();
        if (resolveByIdMethod != null && cachedPluginClass == pluginClass) {
            return resolveByIdMethod;
        }

        try {
            resolveByIdMethod = pluginClass.getMethod("resolveFontEmoji", CommandSender.class, String.class);
            cachedPluginClass = pluginClass;
            return resolveByIdMethod;
        } catch (NoSuchMethodException ignored) {
            resolveByIdMethod = null;
            cachedPluginClass = null;
            return null;
        }
    }

    private static Method resolveTextMethod(Plugin plugin) {
        Class<?> pluginClass = plugin.getClass();
        if (resolveTextMethod != null && cachedPluginClass == pluginClass) {
            return resolveTextMethod;
        }

        try {
            resolveTextMethod = pluginClass.getMethod("resolveFontEmojis", CommandSender.class, String.class);
            cachedPluginClass = pluginClass;
            return resolveTextMethod;
        } catch (NoSuchMethodException ignored) {
            resolveTextMethod = null;
            cachedPluginClass = null;
            return null;
        }
    }
}