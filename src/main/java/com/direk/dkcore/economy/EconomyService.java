package com.direk.dkcore.economy;

import com.direk.dkcore.util.TextUtil;
import org.bukkit.OfflinePlayer;

/**
 * The wallet: the money players carry, from whatever Vault economy is installed.
 * This is separate from the bank (dkBank), which is {@code bank()} / {@link com.direk.dkcore.bank.BankService}.
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

    /**
     * The economy plugin's own formatting, which MAY include its own color codes.
     * To show money to players, use {@link #formatPlain(double)} or
     * {@code Messages.money(...)} so our YAML colors are kept.
     */
    String format(double amount);

    /**
     * Formatted amount ("$1,500") with any color codes removed, so the color around
     * it in messages.yml / gui.yml decides how it looks. Always use this for display.
     */
    default String formatPlain(double amount) {
        String text = format(amount);
        return text == null ? String.valueOf(amount) : TextUtil.stripColors(text);
    }
}
