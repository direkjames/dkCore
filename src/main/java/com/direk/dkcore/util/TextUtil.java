package com.direk.dkcore.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.regex.Pattern;

/**
 * Helpers for text that comes from OTHER plugins (economy formats, PlaceholderAPI values,
 * item names...). Such text often carries old-style color codes ({@code §6}, {@code &6},
 * {@code §x§f§f...}) that would override the colors we set in our YAML files.
 * <p>
 * Author: direk james
 */
public final class TextUtil {

    /** Legacy ampersand codes: {@code &0-&9, &a-&f, &k-&o, &r, &x, &#RRGGBB}. */
    private static final Pattern AMPERSAND_CODE =
            Pattern.compile("&([0-9a-fk-orx]|#[0-9a-f]{6})", Pattern.CASE_INSENSITIVE);

    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer SECTION = LegacyComponentSerializer.legacySection();
    private static final LegacyComponentSerializer AMPERSAND =
            LegacyComponentSerializer.builder().character('&').hexColors().build();

    private TextUtil() {}

    /** Removes section-sign ({@code §}) color/format codes; they're never meant to be visible text. */
    public static String stripSection(String text) {
        if (text == null || text.indexOf('§') < 0) return text;
        return PLAIN.serialize(SECTION.deserialize(text));
    }

    /**
     * Removes both {@code §} and {@code &} color/format codes.
     * Use for text from other plugins that may use ampersand codes (economy formats).
     * Don't use on text players type: {@code "R&D"} would lose {@code "&D"}.
     */
    public static String stripColors(String text) {
        if (text == null) return null;
        String out = stripSection(text);
        if (AMPERSAND_CODE.matcher(out).find()) out = PLAIN.serialize(AMPERSAND.deserialize(out));
        return out;
    }
}
