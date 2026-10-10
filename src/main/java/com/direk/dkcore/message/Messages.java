package com.direk.dkcore.message;

import com.direk.dkcore.config.ConfigFile;
import com.direk.dkcore.economy.EconomyService;
import com.direk.dkcore.util.TextUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.List;

/**
 * Reads MiniMessage strings from a plugin's messages.yml.
 * Use {@code <prefix>} in any message to insert the plugin's prefix.
 * A message set to an empty string is not sent at all.
 * <p>
 * Author: direk james
 */
public final class Messages {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    private final ConfigFile file;

    public Messages(ConfigFile file) {
        this.file = file;
    }

    public void reload() {
        file.reload();
    }

    /** Raw string for a key (lists are joined with new lines). */
    public String raw(String key) {
        if (file.yaml().isList(key)) {
            List<String> lines = file.yaml().getStringList(key);
            return String.join("<newline>", lines);
        }
        String value = file.yaml().getString(key);
        return value == null ? "<red>Missing message: " + key : value;
    }

    public Component get(String key, TagResolver... resolvers) {
        return parse(raw(key), resolvers);
    }

    public Component parse(String miniMessage, TagResolver... resolvers) {
        TagResolver prefix = Placeholder.parsed("prefix", file.yaml().getString("prefix", ""));
        return MM.deserialize(miniMessage, TagResolver.resolver(TagResolver.resolver(resolvers), prefix));
    }

    public void send(Audience to, String key, TagResolver... resolvers) {
        String raw = raw(key);
        if (raw.isEmpty()) return;
        to.sendMessage(parse(raw, resolvers));
    }

    public void actionBar(Audience to, String key, TagResolver... resolvers) {
        String raw = raw(key);
        if (raw.isEmpty()) return;
        to.sendActionBar(parse(raw, resolvers));
    }

    // ---- placeholder shortcuts ----

    /**
     * Plain-text placeholder (player input is never parsed as tags).
     * '§' color codes are removed, so values from other plugins can't recolor our messages.
     */
    public static TagResolver text(String key, Object value) {
        return Placeholder.unparsed(key, TextUtil.stripSection(String.valueOf(value)));
    }

    /**
     * Money placeholder: the economy's format with all of its color codes removed,
     * so the color in the YAML file is what players see. Use this for every amount of money.
     */
    public static TagResolver money(String key, EconomyService economy, double amount) {
        return Placeholder.unparsed(key, economy.formatPlain(amount));
    }

    /** Placeholder whose value may contain MiniMessage formatting. */
    public static TagResolver styled(String key, String miniMessage) {
        return Placeholder.parsed(key, miniMessage);
    }

    public static TagResolver component(String key, Component value) {
        return Placeholder.component(key, value);
    }
}
