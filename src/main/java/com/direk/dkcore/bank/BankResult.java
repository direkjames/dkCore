package com.direk.dkcore.bank;

/**
 * What happened to a bank operation. Author: direk james
 *
 * @param failure null if it worked
 * @param amount  the amount moved (0 if it failed)
 * @param balance the bank balance afterwards (or the balance it had, if it failed)
 */
public record BankResult(Failure failure, double amount, double balance) {

    /** Why an operation didn't happen. Nothing was changed. */
    public enum Failure {
        /** The player has never joined, so there's no bank account. */
        NO_ACCOUNT,
        /** Not enough money in the bank. */
        INSUFFICIENT_FUNDS,
        /** The account would go over its tier's maximum balance. */
        BALANCE_LIMIT,
        /** The amount was zero, negative or too large. */
        INVALID_AMOUNT,
        /** Sending money to the same account. */
        SAME_ACCOUNT,
        /** The database failed, or dkBank isn't running any more (details are in the console). */
        ERROR
    }

    /** @return true if the money moved */
    public boolean success() {
        return failure == null;
    }
}
