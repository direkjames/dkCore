package com.direk.dkcore.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/** Keeps dk_players up to date. Author: direk james */
public final class PlayerListener implements Listener {

    private final PlayerService players;

    public PlayerListener(PlayerService players) {
        this.players = players;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        players.touch(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent e) {
        players.touch(e.getPlayer().getUniqueId(), e.getPlayer().getName());
    }
}
