package com.direk.dkcore.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

/**
 * A YAML file in the plugin folder that is copied from the jar on first run.
 * On every load, keys that exist in the jar's default but are missing from the
 * server's copy are added automatically, so config updates never break old files.
 * <p>
 * Author: direk james
 */
public final class ConfigFile {

    private final JavaPlugin plugin;
    private final String name;
    private final File file;
    private final String[] userSections;
    private YamlConfiguration yaml;

    /**
     * @param userSections sections the server owner fully controls (for example a shop's item list).
     *                     Once such a section exists, missing defaults inside it are NOT re-added,
     *                     so deleted entries stay deleted.
     */
    public ConfigFile(JavaPlugin plugin, String name, String... userSections) {
        this.plugin = plugin;
        this.name = name;
        this.userSections = userSections;
        this.file = new File(plugin.getDataFolder(), name);
        reload();
    }

    public void reload() {
        if (!file.exists() && plugin.getResource(name) != null) {
            plugin.saveResource(name, false);
        }
        yaml = YamlConfiguration.loadConfiguration(file);

        try (InputStream in = plugin.getResource(name)) {
            if (in == null) return;
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(in, StandardCharsets.UTF_8));
            boolean changed = false;
            for (String key : defaults.getKeys(true)) {
                if (defaults.isConfigurationSection(key)) continue;
                if (insideUserSection(key)) continue;
                if (!yaml.contains(key, true)) {
                    yaml.set(key, defaults.get(key));
                    yaml.setComments(key, defaults.getComments(key));
                    changed = true;
                }
            }
            if (changed) {
                save();
                plugin.getLogger().info("Added new default keys to " + name);
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Could not read default " + name, e);
        }
    }

    private boolean insideUserSection(String key) {
        for (String section : userSections) {
            if (key.startsWith(section + ".") && yaml.contains(section, true)) return true;
        }
        return false;
    }

    public void save() {
        try {
            yaml.save(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save " + name, e);
        }
    }

    public YamlConfiguration yaml() {
        return yaml;
    }

    public String name() {
        return name;
    }
}
