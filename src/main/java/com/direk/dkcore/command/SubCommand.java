package com.direk.dkcore.command;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * One sub command, e.g. the "pay" in /flytime pay.
 * Author: direk james
 */
public abstract class SubCommand {

    private final String name;
    private final String permission;
    private final boolean playerOnly;
    private final List<String> aliases;

    protected SubCommand(String name, String permission, boolean playerOnly, String... aliases) {
        this.name = name;
        this.permission = permission;
        this.playerOnly = playerOnly;
        this.aliases = List.of(aliases);
    }

    /** @param args arguments AFTER the sub command name */
    public abstract void execute(CommandSender sender, String[] args);

    /** @param args arguments AFTER the sub command name */
    public List<String> tab(CommandSender sender, String[] args) {
        return List.of();
    }

    public String name() { return name; }
    public String permission() { return permission; }
    public boolean playerOnly() { return playerOnly; }

    public boolean matches(String input) {
        if (name.equalsIgnoreCase(input)) return true;
        for (String a : aliases) if (a.equalsIgnoreCase(input)) return true;
        return false;
    }

    public boolean canUse(CommandSender sender) {
        return permission == null || permission.isEmpty() || sender.hasPermission(permission);
    }
}
