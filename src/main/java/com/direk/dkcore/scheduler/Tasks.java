package com.direk.dkcore.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Small wrapper around the Bukkit scheduler that remembers repeating tasks
 * so they are cancelled automatically when the plugin disables.
 * Author: direk james
 */
public final class Tasks {

    private final Plugin plugin;
    private final List<BukkitTask> repeating = new ArrayList<>();

    public Tasks(Plugin plugin) {
        this.plugin = plugin;
    }

    /** Run on the main thread now (or next tick if called off the main thread). */
    public void sync(Runnable r) {
        if (Bukkit.isPrimaryThread()) r.run();
        else Bukkit.getScheduler().runTask(plugin, r);
    }

    public BukkitTask later(Runnable r, long ticks) {
        return Bukkit.getScheduler().runTaskLater(plugin, r, ticks);
    }

    public BukkitTask async(Runnable r) {
        return Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
    }

    public BukkitTask timer(Runnable r, long delayTicks, long periodTicks) {
        BukkitTask t = Bukkit.getScheduler().runTaskTimer(plugin, r, delayTicks, periodTicks);
        repeating.add(t);
        return t;
    }

    public BukkitTask asyncTimer(Runnable r, long delayTicks, long periodTicks) {
        BukkitTask t = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, r, delayTicks, periodTicks);
        repeating.add(t);
        return t;
    }

    public void cancelAll() {
        for (BukkitTask t : repeating) t.cancel();
        repeating.clear();
    }
}
