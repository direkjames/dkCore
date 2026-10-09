package com.direk.dkcore.afk;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired by an AFK provider (DirekAntiAFK) when a player goes AFK or returns.
 * dk plugins listen to this instead of depending on DirekAntiAFK directly.
 * Author: direk james
 */
public class DkAfkChangeEvent extends PlayerEvent {

    private static final HandlerList HANDLERS = new HandlerList();
    private final boolean afk;

    public DkAfkChangeEvent(@NotNull Player player, boolean afk) {
        super(player);
        this.afk = afk;
    }

    /** true = just went AFK, false = just came back. */
    public boolean isAfk() {
        return afk;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
