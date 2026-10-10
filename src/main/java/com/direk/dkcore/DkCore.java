package com.direk.dkcore;

import com.direk.dkcore.afk.AfkService;
import com.direk.dkcore.bank.BankProvider;
import com.direk.dkcore.config.ConfigFile;
import com.direk.dkcore.database.Database;
import com.direk.dkcore.economy.EconomyProvider;
import com.direk.dkcore.gui.MenuListener;
import com.direk.dkcore.player.PlayerListener;
import com.direk.dkcore.player.PlayerService;
import com.direk.dkcore.startup.StartupBanner;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.logging.Level;

/**
 * dkCore - the shared foundation every dk plugin depends on.
 * Author: direk james
 */
public final class DkCore extends JavaPlugin {

    private static DkCore instance;

    private ConfigFile config;
    private Database database;
    private PlayerService players;
    private EconomyProvider economy;
    private BankProvider bank;
    private StartupBanner banner;

    public static DkCore get() {
        if (instance == null) {
            throw new IllegalStateException("dkCore is not enabled yet");
        }
        return instance;
    }

    @Override
    public void onEnable() {
        long start = System.currentTimeMillis();
        instance = this;
        config = new ConfigFile(this, "config.yml");
        banner = new StartupBanner(this, config.yaml().getBoolean("startup-banner", true));

        database = new Database(this);
        long dbStart = System.currentTimeMillis();
        try {
            database.connect(config.yaml().getConfigurationSection("database"));
            players = new PlayerService(database);
            players.createTables().join();
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Could not connect to the database. Disabling dkCore.", e);
            banner.printFailed(database.describe(), e);
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        long dbMs = System.currentTimeMillis() - dbStart;

        economy = new EconomyProvider(this);
        bank = new BankProvider(this);

        Bukkit.getPluginManager().registerEvents(new PlayerListener(players), this);
        Bukkit.getPluginManager().registerEvents(new MenuListener(), this);
        Bukkit.getPluginManager().registerEvents(banner, this);
        Bukkit.getPluginManager().registerEvents(bank, this);
        bank.linkAfk(); // dkBank enables before dkCore; dkAFK after, which links it then

        banner.printStartup(database.describe(), dbMs, System.currentTimeMillis() - start);
    }

    @Override
    public void onDisable() {
        // Close any open dk menus so items can't be taken out during a reload.
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getOpenInventory().getTopInventory().getHolder() instanceof com.direk.dkcore.gui.Menu) {
                p.closeInventory();
            }
        }
        if (database != null) database.close();
        if (banner != null) banner.printShutdown();
        instance = null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, String @NotNull [] args) {
        MiniMessage mm = MiniMessage.miniMessage();
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            config.reload();
            sender.sendMessage(mm.deserialize("<#7c5cff>dkCore</#7c5cff> <gray>config reloaded. Database changes need a restart."));
            return true;
        }
        sender.sendMessage(mm.deserialize("<#7c5cff>dkCore</#7c5cff> <gray>v" + getPluginMeta().getVersion()
                + " by direk james | db: <white>" + database.dialect()
                + "</white> | economy: <white>" + economy.name()
                + "</white> | bank: <white>" + bank.name() + "</white>"));
        return true;
    }

    public ConfigFile coreConfig() { return config; }
    public Database database() { return database; }
    public PlayerService players() { return players; }
    public EconomyProvider economy() { return economy; }
    /** dkBank for the whole suite: {@code bank().get()} is empty when dkBank isn't installed. */
    public BankProvider bank() { return bank; }
    public boolean debug() { return config.yaml().getBoolean("debug", false); }

    /**
     * Returns true if any registered {@link AfkService} reports the player as AFK.
     * DirekAntiAFK registers one after it migrates to dkCore.
     */
    public boolean isAfk(UUID uuid) {
        for (RegisteredServiceProvider<AfkService> reg :
                Bukkit.getServicesManager().getRegistrations(AfkService.class)) {
            try {
                if (reg.getProvider().isAfk(uuid)) return true;
            } catch (Throwable t) {
                getLogger().log(Level.WARNING, "AFK provider " + reg.getPlugin().getName() + " threw an error", t);
            }
        }
        return false;
    }

    /** True when at least one AFK provider is registered. */
    public boolean hasAfkProvider() {
        return !Bukkit.getServicesManager().getRegistrations(AfkService.class).isEmpty();
    }
}
