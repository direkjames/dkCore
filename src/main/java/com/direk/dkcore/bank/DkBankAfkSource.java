package com.direk.dkcore.bank;

import com.direk.dkcore.DkCore;
import dev.direk.dkbank.api.AfkSource;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

/**
 * Tells dkBank who is AFK, using dkCore's AFK providers (dkAFK). AFK players then earn dkBank's offline
 * interest rate, with no PlaceholderAPI placeholders needed.
 * <p>
 * Registered as dkCore's service, so Bukkit removes it when dkCore disables.
 * Needs dkBank 1.1.0 or newer (where {@link AfkSource} was added).
 * <p>
 * Author: direk james
 */
final class DkBankAfkSource {

    private DkBankAfkSource() {}

    static void register(DkCore core) {
        AfkSource source = core::isAfk;
        Bukkit.getServicesManager().register(AfkSource.class, source, core, ServicePriority.Normal);
    }
}
