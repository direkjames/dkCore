package com.direk.dkcore.bank;

import com.direk.dkcore.util.TextUtil;
import org.bukkit.entity.Player;

import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The bank every dk plugin talks to: the money players keep in dkBank, separate from their wallet
 * ({@link com.direk.dkcore.economy.EconomyService}).
 * <p>
 * dk plugins only compile against dkCore; they never need dkBank on their classpath. Get it with
 * {@code bank().get()} in a DkPlugin or {@code DkCore.get().bank().get()}; it is empty when dkBank isn't
 * installed, so always handle that case:
 * <pre>
 * bank().get().ifPresent(b -&gt; b.give(player.getUniqueId(), 250, "dkFly").thenAccept(result -&gt; {
 *     if (result.success()) messages().send(player, "refund-to-bank");
 * }));
 * </pre>
 * <b>Threads:</b> futures complete on the main thread, so the Bukkit API is safe in {@code thenAccept}.
 * Never call {@code join()} on them from the main thread. Methods that return a value directly read memory
 * and are safe to call often.
 * <p>
 * Amounts are cut to the cent (never rounded up). Every change appears in the player's bank history with
 * {@code source} as who made it.
 * <p>
 * Author: direk james
 */
public interface BankService {

    /** e.g. "dkBank v1.1.0" */
    String name();

    /** The running dkBank version, e.g. 1.1.0 */
    String version();

    /** Balance of an online player, from memory. Empty if the account isn't loaded (offline). */
    OptionalDouble cachedBalance(UUID player);

    /** Loads the balance from the database. Empty if the player has never joined. */
    CompletableFuture<OptionalDouble> balance(UUID player);

    /** Whether the bank holds at least {@code amount} (false if there's no account). */
    CompletableFuture<Boolean> has(UUID player, double amount);

    /** Adds money to the bank, e.g. a reward or refund. Respects the tier's maximum balance. */
    CompletableFuture<BankResult> give(UUID player, double amount, String source);

    /** Takes money from the bank, e.g. a purchase paid from the bank. */
    CompletableFuture<BankResult> take(UUID player, double amount, String source);

    /** Moves money between two bank accounts without fees. The result is the sender's side. */
    CompletableFuture<BankResult> transfer(UUID from, UUID to, double amount);

    /** Id of an online player's tier (from tiers.yml), including permission tiers. Main thread. */
    String tier(Player player);

    /** Display name of an online player's tier, in MiniMessage format. Main thread. */
    String tierName(Player player);

    /** Place on the bank leaderboard (1 = richest), or 0 if not on it. */
    int rank(UUID player);

    /** Whether dkBank's alt-account limit locks this player's bank. Main thread. */
    boolean isLocked(Player player);

    /** Formats money the way dkBank shows it, e.g. $1,234.56. May contain dkBank's own colors. */
    String format(double amount);

    /**
     * Same as {@link #format(double)} but with color codes removed, so the color in our YAML
     * files decides how it looks. Always use this to show bank money to players.
     */
    default String formatPlain(double amount) {
        String text = format(amount);
        return text == null ? String.valueOf(amount) : TextUtil.stripColors(text);
    }
}
