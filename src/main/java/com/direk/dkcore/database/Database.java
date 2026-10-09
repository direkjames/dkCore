package com.direk.dkcore.database;

import com.direk.dkcore.DkCore;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * The one database shared by every dk plugin.
 * <p>
 * All queries run on a single background thread, in the order they were submitted.
 * That keeps SQLite happy and means a "load" can never overtake an earlier "save"
 * for the same player. Never call {@code .join()} on the main thread.
 * <p>
 * Author: direk james
 */
public final class Database {

    private final DkCore core;
    private HikariDataSource source;
    private Dialect dialect;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "dkCore-Database");
        t.setDaemon(true);
        return t;
    });

    public Database(DkCore core) {
        this.core = core;
    }

    public void connect(ConfigurationSection cfg) {
        HikariConfig hc = new HikariConfig();
        hc.setPoolName("dkCore");
        String type = cfg == null ? "sqlite" : cfg.getString("type", "sqlite");

        if ("mysql".equalsIgnoreCase(type)) {
            dialect = Dialect.MYSQL;
            ConfigurationSection m = cfg.getConfigurationSection("mysql");
            String url = "jdbc:mysql://" + m.getString("host", "localhost") + ":" + m.getInt("port", 3306)
                    + "/" + m.getString("database", "dksuite");
            hc.setJdbcUrl(url);
            hc.setDriverClassName("com.mysql.cj.jdbc.Driver");
            hc.setUsername(m.getString("username", "root"));
            hc.setPassword(m.getString("password", ""));
            hc.setMaximumPoolSize(Math.max(2, m.getInt("pool-size", 8)));
            ConfigurationSection props = m.getConfigurationSection("properties");
            if (props != null) {
                for (String k : props.getKeys(false)) hc.addDataSourceProperty(k, String.valueOf(props.get(k)));
            }
        } else {
            dialect = Dialect.SQLITE;
            core.getDataFolder().mkdirs();
            File file = new File(core.getDataFolder(), cfg == null ? "dk-suite.db" : cfg.getString("sqlite-file", "dk-suite.db"));
            hc.setJdbcUrl("jdbc:sqlite:" + file.getAbsolutePath());
            hc.setDriverClassName("org.sqlite.JDBC");
            hc.setMaximumPoolSize(1);
            hc.setConnectionTestQuery("SELECT 1");
        }
        source = new HikariDataSource(hc);
    }

    public Dialect dialect() {
        return dialect;
    }

    /** Runs a query on the database thread and returns its result. */
    public <T> CompletableFuture<T> query(SqlFunction<Connection, T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection c = source.getConnection()) {
                return work.apply(c);
            } catch (SQLException e) {
                core.getLogger().log(Level.SEVERE, "Database error", e);
                throw new CompletionException(e);
            }
        }, executor);
    }

    /** Runs an update on the database thread. */
    public CompletableFuture<Void> execute(SqlConsumer<Connection> work) {
        return query(c -> {
            work.accept(c);
            return null;
        });
    }

    /** Creates tables if they don't exist. Each string is one CREATE TABLE statement. */
    public CompletableFuture<Void> createTables(List<String> statements) {
        return execute(c -> {
            try (var st = c.createStatement()) {
                for (String sql : statements) st.execute(sql);
            }
        });
    }

    /**
     * Builds an "insert or update" statement for the current database.
     * Example: upsert("dkfly_players", "uuid", "uuid", "seconds", "enabled")
     */
    public String upsert(String table, String key, String... columns) {
        StringBuilder cols = new StringBuilder();
        StringBuilder marks = new StringBuilder();
        StringBuilder updates = new StringBuilder();
        for (int i = 0; i < columns.length; i++) {
            if (i > 0) { cols.append(", "); marks.append(", "); }
            cols.append(columns[i]);
            marks.append('?');
            if (!columns[i].equals(key)) {
                if (!updates.isEmpty()) updates.append(", ");
                updates.append(dialect == Dialect.MYSQL
                        ? columns[i] + " = VALUES(" + columns[i] + ")"
                        : columns[i] + " = excluded." + columns[i]);
            }
        }
        String base = "INSERT INTO " + table + " (" + cols + ") VALUES (" + marks + ")";
        return dialect == Dialect.MYSQL
                ? base + " ON DUPLICATE KEY UPDATE " + updates
                : base + " ON CONFLICT(" + key + ") DO UPDATE SET " + updates;
    }

    /** Waits for queued work to finish, then closes the pool. Called by dkCore on shutdown. */
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(15, TimeUnit.SECONDS)) {
                core.getLogger().warning("Database queue did not finish within 15s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (source != null) source.close();
    }

    public enum Dialect { SQLITE, MYSQL }

    @FunctionalInterface
    public interface SqlFunction<A, R> {
        R apply(A a) throws SQLException;
    }

    @FunctionalInterface
    public interface SqlConsumer<A> {
        void accept(A a) throws SQLException;
    }
}
