package net.enelson.sopprefix;

import net.enelson.sopprefix.command.PrefixCommand;
import net.enelson.sopprefix.gui.PrefixMenuListener;
import net.enelson.sopprefix.gui.PrefixMenuService;
import net.enelson.sopprefix.gui.SuffixMenuListener;
import net.enelson.sopprefix.gui.SuffixMenuService;
import net.enelson.sopprefix.command.SuffixCommand;
import net.enelson.sopprefix.placeholder.SopPrefixPlaceholders;
import net.enelson.sopprefix.prefix.PrefixManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class SopPrefixPlugin extends JavaPlugin {

    private PrefixManager prefixManager;
    private PrefixMenuService prefixMenuService;
    private SuffixMenuService suffixMenuService;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.prefixManager = new PrefixManager(this);
        this.prefixManager.reload();

        this.prefixMenuService = new PrefixMenuService(this.prefixManager);
        this.suffixMenuService = new SuffixMenuService(this.prefixManager);

        PrefixCommand prefixCommand = new PrefixCommand(this.prefixManager, this.prefixMenuService);
        PluginCommand prefixPluginCommand = getCommand("prefix");
        if (prefixPluginCommand != null) {
            prefixPluginCommand.setExecutor(prefixCommand);
            prefixPluginCommand.setTabCompleter(prefixCommand);
        }

        SuffixCommand suffixCommand = new SuffixCommand(this.prefixManager, this.suffixMenuService);
        PluginCommand suffixPluginCommand = getCommand("suffix");
        if (suffixPluginCommand != null) {
            suffixPluginCommand.setExecutor(suffixCommand);
            suffixPluginCommand.setTabCompleter(suffixCommand);
        }

        getServer().getPluginManager().registerEvents(this.prefixManager, this);
        getServer().getPluginManager().registerEvents(new PrefixMenuListener(this.prefixMenuService), this);
        getServer().getPluginManager().registerEvents(new SuffixMenuListener(this.suffixMenuService), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new SopPrefixPlaceholders(this.prefixManager).register();
        }
    }

    @Override
    public void onDisable() {
        if (this.prefixMenuService != null) {
            this.prefixMenuService.closeAll();
        }
        if (this.suffixMenuService != null) {
            this.suffixMenuService.closeAll();
        }
        if (this.prefixManager != null) {
            this.prefixManager.shutdown();
        }
    }
}
