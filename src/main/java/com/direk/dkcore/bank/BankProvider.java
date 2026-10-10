package com.direk.dkcore.bank;

import com.direk.dkcore.DkCore;
import com.direk.dkcore.afk.AfkService;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.logging.Level;

/**
 * Connects the dk suite to dkBank.
 * <ul>
 *     <li>{@link #get()}: the bank for any dk plugin, checked fresh each time so load order never matters</li>
 *     <li>links dkCore's AFK providers (dkAFK) to dkBank's interest, as soon as both are running</li>
 * </ul>
 * dkBank stays a standalone plugin: it never needs dkCore. dkCore lists dkBank as a softdepend, so dkBank
 * enables first and its API is ready when dkCore starts.
 * <p>
 * Author: direk james
 */
public final class BankProvider implements Listener {

    public static final String DKBANK = "dkBank";

    private final DkCore core;
    private BankService bridge;
    private Object bridgeApi; // the DkBankAPI instance the bridge wraps (Object so dkBank stays optional)
    private boolean afkLinked;
    private boolean afkUnsupported;
    private boolean warned;

    public BankProvider(DkCore core) {
        this.core = core;
    }

    /** dkBank's bank, or empty if dkBank isn't installed or enabled. */
    public Optional<BankService> get() {
        if (!Bukkit.getPluginManager().isPluginEnabled(DKBANK)) return Optional.empty();
        try {
            Object api = DkBankBridge.currentApi();
            if (api == null) return Optional.empty();
            if (bridge == null || bridgeApi != api) {
                bridgeApi = api;
                bridge = DkBankBridge.wrap(api);
            }
            return Optional.of(bridge);
        } catch (LinkageError e) {
            if (!warned) {
                warned = true;
                core.getLogger().log(Level.WARNING, "dkBank is installed but its API could not be loaded. "
                        + "Update dkBank to 1.1.0 or newer.", e);
            }
            return Optional.empty();
        }
    }

    /** True when dkBank is running. */
    public boolean available() {
        return get().isPresent();
    }

    /** e.g. "dkBank v1.1.0", or "none". */
    public String name() {
        return get().map(BankService::name).orElse("none");
    }

    /** dkBank's plugin if it's installed (enabled or not), for the startup banner. */
    public Plugin plugin() {
        return Bukkit.getPluginManager().getPlugin(DKBANK);
    }

    /** True once dkBank asks dkCore (dkAFK) who is AFK. */
    public boolean afkLinked() {
        return afkLinked;
    }

    /**
     * Lets dkBank ask dkCore who is AFK, once dkBank is running and an AFK provider (dkAFK) has registered.
     * Called when dkCore enables and again whenever either of them shows up later.
     */
    public void linkAfk() {
        if (afkLinked || afkUnsupported || !core.hasAfkProvider() || !Bukkit.getPluginManager().isPluginEnabled(DKBANK)) return;
        try {
            DkBankAfkSource.register(core);
            afkLinked = true;
            if (core.debug()) core.getLogger().info("[debug] Linked the AFK provider to dkBank");
        } catch (LinkageError e) {
            afkUnsupported = true; // don't try (and warn) again on every event
            core.getLogger().warning("This dkBank is too old to use dkAFK for interest. Update dkBank to 1.1.0 or newer.");
        }
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent e) {
        if (e.getPlugin().getName().equals(DKBANK)) linkAfk();
    }

    @EventHandler
    public void onServiceRegister(ServiceRegisterEvent e) {
        if (e.getProvider().getService() == AfkService.class) linkAfk();
    }
}
