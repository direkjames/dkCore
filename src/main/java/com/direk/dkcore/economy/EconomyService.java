package com.direk.dkcore.economy;

import org.bukkit.OfflinePlayer;

/**
 * The wallet every dk plugin talks to (the money players carry), backed by Vault.
 * Another plugin can register its own implementation through Bukkit's ServicesManager to take over.
 * Money kept in dkBank is separate: use {@link com.direk.dkcore.bank.BankService} for that.
 * <p>
 * All methods must be called on the main thread.
 * Author: direk james
 */
public interface EconomyService {

    String name();

    double balance(OfflinePlayer player);

    boolean has(OfflinePlayer player, double amount);

    /** @return true if the money was taken */
    boolean withdraw(OfflinePlayer player, double amount);

    /** @return true if the money was given */
    boolean deposit(OfflinePlayer player, double amount);

    String format(double amount);
}
