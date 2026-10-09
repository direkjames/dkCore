package com.direk.dkcore.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses durations like "90", "45s", "10m", "1h30m", "2d 4h".
 * A plain number means seconds.
 * Author: direk james
 */
public final class TimeUtil {

    private static final Pattern PART = Pattern.compile("(\\d+)\\s*([dhms])");
    private static final Pattern FULL = Pattern.compile("^(\\s*\\d+\\s*[dhms])+\\s*$");

    private TimeUtil() {}

    /** @return seconds, or -1 if the text is not a valid duration */
    public static long parseSeconds(String text) {
        if (text == null) return -1;
        String t = text.trim().toLowerCase(Locale.ROOT);
        if (t.isEmpty()) return -1;
        try {
            if (t.chars().allMatch(Character::isDigit)) return Long.parseLong(t);
            if (!FULL.matcher(t).matches()) return -1;
            long total = 0;
            Matcher m = PART.matcher(t);
            while (m.find()) {
                long n = Long.parseLong(m.group(1));
                long unit = switch (m.group(2)) {
                    case "d" -> 86_400L;
                    case "h" -> 3_600L;
                    case "m" -> 60L;
                    default -> 1L;
                };
                total = Math.addExact(total, Math.multiplyExact(n, unit));
            }
            return total;
        } catch (NumberFormatException | ArithmeticException e) {
            return -1;
        }
    }

    /** Like parseSeconds, but returns {@code fallback} for invalid text. "none"/"0" give 0. */
    public static long parseOr(String text, long fallback) {
        if (text != null && text.trim().equalsIgnoreCase("none")) return 0;
        long v = parseSeconds(text);
        return v < 0 ? fallback : v;
    }
}
