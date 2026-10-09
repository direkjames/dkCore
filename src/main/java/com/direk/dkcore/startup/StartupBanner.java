package com.direk.dkcore.startup;

import com.direk.dkcore.DkCore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * The dkCore banner in the console:
 * - on enable: logo, version, server, Java, database, startup time
 * - once the server finishes loading: every dk plugin and the services they use
 * <p>
 * The logo is sent as plain text components (never parsed as MiniMessage), and all
 * dynamic values go in as unparsed placeholders, so characters like {@code \} and {@code <} can't
 * break the formatting.
 * <p>
 * Turn it off with "startup-banner: false" in config.yml.
 * Author: direk james
 */
public final class StartupBanner implements Listener {

    private static final String[] LOGO = {
            "     _  _      ____                    ",
            "  __| || | __ / ___|  ___   _ __   ___ ",
            " / _` || |/ /| |     / _ \\ | '__| / _ \\",
            "| (_| ||   < | |___ | (_) || |   |  __/",
            " \\__,_||_|\\_\\ \\____| \\___/ |_|    \\___|"
    };

    private static final TextColor PURPLE = TextColor.color(0x7c5cff);
    private static final TextColor BLUE = TextColor.color(0x7cc6ff);
    private static final String LINE = "<dark_gray>" + "-".repeat(56);
    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final DkCore core;
    private final boolean enabled;

    public StartupBanner(DkCore core, boolean enabled) {
        this.core = core;
        this.enabled = enabled;
    }

    /** Printed as soon as dkCore has enabled. */
    public void printStartup(String database, long dbMs, long totalMs) {
        if (!enabled) {
            core.getLogger().info("dkCore v" + version() + " enabled (" + database + ") in " + totalMs + " ms");
            return;
        }
        blank();
        line(LINE);
        logo();
        blank();
        line("  <white><bold>dkCore</bold></white> <gray>v<v></gray>  <dark_gray>|</dark_gray>  <gray>by</gray> <#7cc6ff>direk james",
                v(version()));
        line("  <dark_gray><italic>The shared core of the dk plugin suite");
        line(LINE);
        row("Server", "<v>", v(Bukkit.getName() + " " + Bukkit.getMinecraftVersion()));
        row("Java", "<v>", v(Runtime.version() + " (" + System.getProperty("java.vendor", "?") + ")"));
        row("Database", "<green><v></green> <dark_gray>connected in <ms> ms", v(database), t("ms", dbMs));
        row("Status", "<green>Enabled</green> <dark_gray>in <ms> ms", t("ms", totalMs));
        row("Plugins", "<dark_gray>listed when the server finishes loading...");
        line(LINE);
        blank();
    }

    /** Printed instead when the database could not be reached. */
    public void printFailed(String database, Exception error) {
        String red = "<red>" + "-".repeat(56);
        blank();
        line(red);
        line("  <white><bold>dkCore</bold></white> <gray>v<v></gray>  <red><bold>FAILED TO START", v(version()));
        row("Database", "<red><v> could not connect", v(database));
        row("Reason", "<red><v>", v(String.valueOf(error.getMessage())));
        row("Fix", "Check database settings in plugins/dkCore/config.yml");
        line("  <red>Every dk plugin will be disabled until this is fixed.");
        line(red);
        blank();
    }

    /** Runs after every plugin has enabled, so all dk plugins and services are known. */
    @EventHandler
    public void onServerLoad(ServerLoadEvent e) {
        if (!enabled) return;
        line(LINE);
        line("  <gradient:#7c5cff:#7cc6ff><bold>dk suite</bold></gradient> <gray>ready");
        line(LINE);

        List<Plugin> dk = dkPlugins();
        if (dk.isEmpty()) {
            row("Plugins", "<dark_gray>none installed yet");
        } else {
            boolean first = true;
            for (Plugin p : dk) {
                String status = p.isEnabled() ? "<green>OK" : "<red>DISABLED";
                row(first ? "Plugins" : "", "<white><name></white> <gray>v<v></gray>  " + status,
                        t("name", p.getName()), v(p.getPluginMeta().getVersion()));
                first = false;
            }
        }

        String eco = core.economy().name();
        if (eco.equals("none")) row("Economy", "<yellow>none found (shops and payments are off)");
        else row("Economy", "<green><v>", v(eco));
        row("AFK", core.hasAfkProvider()
                ? "<green>provider registered"
                : "<gray>no provider (plugins use their fallback)");
        line(LINE);
    }

    /** Prints a short goodbye on shutdown. */
    public void printShutdown() {
        if (!enabled) return;
        line("<dark_gray>[</dark_gray><gradient:#7c5cff:#7cc6ff>dkCore</gradient><dark_gray>]</dark_gray> "
                + "<gray>v<v> disabled. All data saved.", v(version()));
    }

    // ---- helpers ----

    /** Every plugin that depends on dkCore, in load order. */
    private List<Plugin> dkPlugins() {
        List<Plugin> list = new ArrayList<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            if (p == core) continue;
            List<String> deps = p.getPluginMeta().getPluginDependencies();
            List<String> soft = p.getPluginMeta().getPluginSoftDependencies();
            if (deps.contains(core.getName()) || soft.contains(core.getName())) list.add(p);
        }
        return list;
    }

    /** Logo lines as plain text, colored purple → blue from top to bottom. */
    private void logo() {
        ConsoleCommandSender console = Bukkit.getConsoleSender();
        for (int i = 0; i < LOGO.length; i++) {
            float progress = LOGO.length == 1 ? 0f : (float) i / (LOGO.length - 1);
            console.sendMessage(Component.text(LOGO[i], TextColor.lerp(progress, PURPLE, BLUE)));
        }
    }

    private void row(String label, String valueMiniMessage, TagResolver... values) {
        line("  <#7cc6ff>" + String.format("%-10s", label) + "</#7cc6ff><gray>" + valueMiniMessage, values);
    }

    private void line(String miniMessage, TagResolver... values) {
        Bukkit.getConsoleSender().sendMessage(MM.deserialize(miniMessage, TagResolver.resolver(values)));
    }

    private void blank() {
        Bukkit.getConsoleSender().sendMessage(Component.text(" "));
    }

    /** A value placeholder {@code <v>} shown exactly as written. */
    private static TagResolver v(String value) {
        return Placeholder.unparsed("v", value);
    }

    private static TagResolver t(String key, Object value) {
        return Placeholder.unparsed(key, String.valueOf(value));
    }

    private String version() {
        return core.getPluginMeta().getVersion();
    }
}
