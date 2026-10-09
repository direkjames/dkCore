package com.direk.dkcore.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;

/**
 * Bridges dkCore's EconomyService to whatever Vault economy is installed
 * (dkBank today). Only loaded when Vault is present.
 * Author: direk james
 */
final class VaultEconomyService implements EconomyService {

    private final Economy eco;

    VaultEconomyService(Economy eco) {
        this.eco = eco;
    }

    @Override
    public String name() {
        return "Vault (" + eco.getName() + ")";
    }

    @Override
    public double balance(OfflinePlayer player) {
        return eco.getBalance(player);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return eco.has(player, amount);
    }

    @Override
    public boolean withdraw(OfflinePlayer player, double amount) {
        if (amount <= 0) return true;
        return eco.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public boolean deposit(OfflinePlayer player, double amount) {
        if (amount <= 0) return true;
        return eco.depositPlayer(player, amount).transactionSuccess();
    }

    @Override
    public String format(double amount) {
        return eco.format(amount);
    }
}
