package com.direk.dkcore.util;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Turns seconds into text using a config section like:
 * <pre>
 * style: compact          # compact = "1h 5m 3s", digital = "01:05:03"
 * separator: ' '
 * hide-zero-units: true   # compact only: "1h 3s" instead of "1h 0m 3s"
 * zero: '0s'
 * labels: {day: 'd', hour: 'h', minute: 'm', second: 's'}
 * </pre>
 * Author: direk james
 */
public final class TimeFormatter {

    private final boolean digital;
    private final String separator;
    private final boolean hideZero;
    private final String zero;
    private final String day, hour, minute, second;

    public TimeFormatter(ConfigurationSection s) {
        this.digital = s != null && "digital".equalsIgnoreCase(s.getString("style", "compact"));
        this.separator = s == null ? " " : s.getString("separator", " ");
        this.hideZero = s == null || s.getBoolean("hide-zero-units", true);
        this.zero = s == null ? "0s" : s.getString("zero", "0s");
        this.day = s == null ? "d" : s.getString("labels.day", "d");
        this.hour = s == null ? "h" : s.getString("labels.hour", "h");
        this.minute = s == null ? "m" : s.getString("labels.minute", "m");
        this.second = s == null ? "s" : s.getString("labels.second", "s");
    }

    public String format(long totalSeconds) {
        if (totalSeconds <= 0) return zero;
        long d = totalSeconds / 86_400;
        long h = (totalSeconds % 86_400) / 3_600;
        long m = (totalSeconds % 3_600) / 60;
        long s = totalSeconds % 60;

        if (digital) {
            String hms = (d * 24 + h) > 0
                    ? String.format("%02d:%02d:%02d", d * 24 + h, m, s)
                    : String.format("%02d:%02d", m, s);
            return hms;
        }

        StringBuilder out = new StringBuilder();
        append(out, d, day, true);
        append(out, h, hour, d > 0);
        append(out, m, minute, d > 0 || h > 0);
        append(out, s, second, true);
        return out.toString();
    }

    private void append(StringBuilder out, long value, String label, boolean shownIfZero) {
        if (value == 0 && (hideZero || !shownIfZero || out.isEmpty())) return;
        if (!out.isEmpty()) out.append(separator);
        out.append(value).append(label);
    }
}
