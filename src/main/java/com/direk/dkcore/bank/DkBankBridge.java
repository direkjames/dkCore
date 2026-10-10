package com.direk.dkcore.bank;

import dev.direk.dkbank.api.DkBankAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.math.BigDecimal;
import java.util.OptionalDouble;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Bridges dkCore's {@link BankService} to dkBank's Developer API.
 * <p>
 * This and {@link DkBankAfkSource} are the only classes that touch dkBank's types. They are only loaded
 * once dkBank is known to be enabled, so dkCore still starts on a server without dkBank.
 * <p>
 * Author: direk james
 */
final class DkBankBridge implements BankService {

    private final DkBankAPI api;

    private DkBankBridge(DkBankAPI api) {
        this.api = api;
    }

    /** The API dkBank registered, or null if it hasn't (yet). Returned as Object so callers never load dkBank types. */
    static Object currentApi() {
        RegisteredServiceProvider<DkBankAPI> reg = Bukkit.getServicesManager().getRegistration(DkBankAPI.class);
        return reg == null ? null : reg.getProvider();
    }

    static BankService wrap(Object api) {
        return new DkBankBridge((DkBankAPI) api);
    }

    @Override
    public String name() {
        return "dkBank v" + api.version();
    }

    @Override
    public String version() {
        return api.version();
    }

    @Override
    public OptionalDouble cachedBalance(UUID player) {
        return api.cachedBalance(player).map(b -> OptionalDouble.of(b.doubleValue())).orElse(OptionalDouble.empty());
    }

    @Override
    public CompletableFuture<OptionalDouble> balance(UUID player) {
        return api.account(player).thenApply(a -> a.map(acc -> OptionalDouble.of(acc.balance().doubleValue()))
                .orElse(OptionalDouble.empty()));
    }

    @Override
    public CompletableFuture<Boolean> has(UUID player, double amount) {
        if (!valid(amount)) return CompletableFuture.completedFuture(false);
        return api.has(player, money(amount));
    }

    @Override
    public CompletableFuture<BankResult> give(UUID player, double amount, String source) {
        if (!valid(amount)) return invalid();
        return api.give(player, money(amount), source).thenApply(DkBankBridge::convert);
    }

    @Override
    public CompletableFuture<BankResult> take(UUID player, double amount, String source) {
        if (!valid(amount)) return invalid();
        return api.take(player, money(amount), source).thenApply(DkBankBridge::convert);
    }

    @Override
    public CompletableFuture<BankResult> transfer(UUID from, UUID to, double amount) {
        if (!valid(amount)) return invalid();
        return api.transfer(from, to, money(amount)).thenApply(DkBankBridge::convert);
    }

    @Override
    public String tier(Player player) {
        return api.tierOf(player).id();
    }

    @Override
    public String tierName(Player player) {
        return api.tierOf(player).displayName();
    }

    @Override
    public int rank(UUID player) {
        return api.rank(player);
    }

    @Override
    public boolean isLocked(Player player) {
        return api.isLocked(player);
    }

    @Override
    public String format(double amount) {
        return api.format(Double.isFinite(amount) ? money(amount) : BigDecimal.ZERO);
    }

    // ---- helpers ----

    /** NaN and infinity would throw in BigDecimal; dkBank itself rejects zero, negative and too large. */
    private static boolean valid(double amount) {
        return Double.isFinite(amount);
    }

    /** valueOf uses the double's shortest text (0.1 stays 0.1), so cutting to the cent doesn't lose a cent. */
    private static BigDecimal money(double amount) {
        return BigDecimal.valueOf(amount);
    }

    private static CompletableFuture<BankResult> invalid() {
        return CompletableFuture.completedFuture(new BankResult(BankResult.Failure.INVALID_AMOUNT, 0, 0));
    }

    private static BankResult convert(dev.direk.dkbank.api.BankResult r) {
        BankResult.Failure failure = null;
        if (r.failure() != null) {
            try {
                failure = BankResult.Failure.valueOf(r.failure().name());
            } catch (IllegalArgumentException e) {
                failure = BankResult.Failure.ERROR; // a reason added in a newer dkBank
            }
        }
        return new BankResult(failure, r.amount().doubleValue(), r.balance().doubleValue());
    }
}
