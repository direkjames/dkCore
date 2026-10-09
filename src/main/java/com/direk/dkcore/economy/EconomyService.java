package com.direk.dkcore.economy;

import org.bukkit.OfflinePlayer;

/**
 * The money system every dk plugin talks to.
 * Right now it is backed by Vault. When dkBank migrates to dkCore it will
 * register its own implementation through Bukkit's ServicesManager and take over.
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
