package com.direk.dkcore;

import com.direk.dkcore.command.DkCommand;
import com.direk.dkcore.config.ConfigFile;
import com.direk.dkcore.database.Database;
import com.direk.dkcore.economy.EconomyProvider;
import com.direk.dkcore.message.Messages;
import com.direk.dkcore.scheduler.Tasks;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

/**
 * Base class for every dk plugin. Extend this instead of JavaPlugin.
 * Gives each plugin its config.yml, messages.yml, database access,
 * command registration and scheduler helpers for free.
 * <p>
 * Author: direk james
 */
public abstract class DkPlugin extends JavaPlugin {

    private DkCore core;
    private ConfigFile config;
    private Messages messages;
    private Tasks tasks;

    @Override
    public final void onEnable() {
        try {
            core = DkCore.get();
        } catch (IllegalStateException e) {
            getLogger().severe("dkCore is missing or failed to start. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        config = new ConfigFile(this, "config.yml", userConfigSections());
        messages = new Messages(new ConfigFile(this, "messages.yml"));
        tasks = new Tasks(this);
        try {
            enable();
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Failed to enable " + getName(), t);
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public final void onDisable() {
        try {
            disable();
        } catch (Throwable t) {
            getLogger().log(Level.SEVERE, "Error while disabling " + getName(), t);
        }
        if (tasks != null) tasks.cancelAll();
    }

    /**
     * Config sections the server owner fully controls (e.g. "shop.items").
     * Missing defaults inside these are not re-added on load.
     */
    protected String[] userConfigSections() { return new String[0]; }

    /** Called when the plugin enables, after dkCore services are ready. */
    protected abstract void enable();

    /** Called when the plugin disables. Save data here. */
    protected void disable() {}

    /** Reloads config.yml and messages.yml. Override to reload more, but call super. */
    public void reloadAll() {
        config.reload();
        messages.reload();
    }

    // ---- helpers ----

    public DkCore core() { return core; }
    public ConfigFile cfg() { return config; }
    public Messages messages() { return messages; }
    public Tasks tasks() { return tasks; }
    public Database database() { return core.database(); }
    public EconomyProvider economy() { return core.economy(); }

    public void listen(Listener... listeners) {
        for (Listener l : listeners) Bukkit.getPluginManager().registerEvents(l, this);
    }

    /** Binds a command declared in plugin.yml to a {@link DkCommand}. */
    public void registerCommand(DkCommand command) {
        PluginCommand pc = getCommand(command.name());
        if (pc == null) {
            getLogger().warning("Command /" + command.name() + " is missing from plugin.yml");
            return;
        }
        pc.setExecutor(command);
        pc.setTabCompleter(command);
    }

    public void debug(String msg) {
        if (core.debug()) getLogger().info("[debug] " + msg);
    }
}
