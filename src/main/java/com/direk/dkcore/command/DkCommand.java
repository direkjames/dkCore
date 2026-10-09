package com.direk.dkcore.command;

import com.direk.dkcore.message.Messages;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A root command with optional sub commands.
 * If the first argument matches a registered sub command it runs; otherwise
 * {@link #root(CommandSender, String[])} runs.
 * <p>
 * Uses these message keys from the plugin's messages.yml:
 * no-permission, player-only, unknown-player.
 * <p>
 * Author: direk james
 */
public abstract class DkCommand implements TabExecutor {

    private final String name;
    private final String permission;
    protected final Messages messages;
    private final Map<String, SubCommand> subs = new LinkedHashMap<>();

    protected DkCommand(String name, String permission, Messages messages) {
        this.name = name;
        this.permission = permission;
        this.messages = messages;
    }

    protected void sub(SubCommand sub) {
        subs.put(sub.name().toLowerCase(Locale.ROOT), sub);
    }

    /** Runs when no sub command matched. */
    protected abstract void root(CommandSender sender, String[] args);

    /** Tab completion for the root command (first argument), merged with sub command names. */
    protected List<String> rootTab(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        if (permission != null && !sender.hasPermission(permission)) {
            messages.send(sender, "no-permission");
            return true;
        }
        if (args.length > 0) {
            SubCommand sub = find(args[0]);
            if (sub != null) {
                if (!sub.canUse(sender)) {
                    messages.send(sender, "no-permission");
                } else if (sub.playerOnly() && !(sender instanceof Player)) {
                    messages.send(sender, "player-only");
                } else {
                    sub.execute(sender, Arrays.copyOfRange(args, 1, args.length));
                }
                return true;
            }
        }
        root(sender, args);
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, String @NotNull [] args) {
        if (permission != null && !sender.hasPermission(permission)) return List.of();
        if (args.length == 1) {
            List<String> out = new ArrayList<>(rootTab(sender, args));
            for (SubCommand s : subs.values()) {
                if (s.canUse(sender)) out.add(s.name());
            }
            return filter(out, args[0]);
        }
        SubCommand sub = find(args[0]);
        if (sub != null && sub.canUse(sender)) {
            String[] rest = Arrays.copyOfRange(args, 1, args.length);
            return filter(sub.tab(sender, rest), args[args.length - 1]);
        }
        return filter(rootTab(sender, args), args[args.length - 1]);
    }

    private SubCommand find(String input) {
        for (SubCommand s : subs.values()) if (s.matches(input)) return s;
        return null;
    }

    public String name() { return name; }

    // ---- helpers for implementations ----

    public static List<String> filter(List<String> options, String typed) {
        String t = typed.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) if (o.toLowerCase(Locale.ROOT).startsWith(t)) out.add(o);
        return out;
    }

    public static List<String> onlineNames() {
        List<String> names = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
        return names;
    }
}
