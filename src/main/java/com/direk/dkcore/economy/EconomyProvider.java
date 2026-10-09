package com.direk.dkcore.economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Optional;

/**
 * Finds the active economy, checked fresh each time so load order never matters:
 * 1. a dk EconomyService registered with the ServicesManager (dkBank, later)
 * 2. otherwise Vault's Economy
 * <p>
 * Author: direk james
 */
public final class EconomyProvider {

    private final Plugin core;
    private EconomyService vaultBridge;
    private Object vaultEconomy; // Economy instance the bridge wraps (kept as Object so Vault stays optional)

    public EconomyProvider(Plugin core) {
        this.core = core;
    }

    public Optional<EconomyService> get() {
        RegisteredServiceProvider<EconomyService> dk = Bukkit.getServicesManager().getRegistration(EconomyService.class);
        if (dk != null) return Optional.of(dk.getProvider());
        return Optional.ofNullable(vault());
    }

    public String name() {
        return get().map(EconomyService::name).orElse("none");
    }

    private EconomyService vault() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) return null;
        try {
            RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                    Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (rsp == null) return null;
            if (vaultBridge == null || vaultEconomy != rsp.getProvider()) {
                vaultEconomy = rsp.getProvider();
                vaultBridge = new VaultEconomyService(rsp.getProvider());
            }
            return vaultBridge;
        } catch (NoClassDefFoundError e) {
            core.getLogger().warning("Vault is installed but its API could not be loaded.");
            return null;
        }
    }
}
