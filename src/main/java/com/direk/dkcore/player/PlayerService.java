package com.direk.dkcore.player;

import com.direk.dkcore.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Shared player table (dk_players) used by every dk plugin:
 * UUID, last known name, first join and last seen.
 * Lets any plugin look up offline players by name.
 * <p>
 * Author: direk james
 */
public final class PlayerService {

    private final Database db;

    public PlayerService(Database db) {
        this.db = db;
    }

    public CompletableFuture<Void> createTables() {
        return db.createTables(List.of(
                "CREATE TABLE IF NOT EXISTS dk_players ("
                        + "uuid VARCHAR(36) NOT NULL PRIMARY KEY, "
                        + "name VARCHAR(16) NOT NULL, "
                        + "name_lower VARCHAR(16) NOT NULL, "
                        + "first_join BIGINT NOT NULL, "
                        + "last_seen BIGINT NOT NULL)"
        ));
    }

    /** Records a join. Keeps first_join, updates name and last_seen. */
    public CompletableFuture<Void> touch(UUID uuid, String name) {
        long now = System.currentTimeMillis();
        return db.execute(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "UPDATE dk_players SET name = ?, name_lower = ?, last_seen = ? WHERE uuid = ?")) {
                ps.setString(1, name);
                ps.setString(2, name.toLowerCase(Locale.ROOT));
                ps.setLong(3, now);
                ps.setString(4, uuid.toString());
                if (ps.executeUpdate() > 0) return;
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO dk_players (uuid, name, name_lower, first_join, last_seen) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, uuid.toString());
                ps.setString(2, name);
                ps.setString(3, name.toLowerCase(Locale.ROOT));
                ps.setLong(4, now);
                ps.setLong(5, now);
                ps.executeUpdate();
            }
        });
    }

    /**
     * Finds a player by name: online first, then the server's user cache,
     * then the dk_players table. Completes with empty if they never joined.
     */
    public CompletableFuture<Optional<PlayerRef>> find(String name) {
        var online = Bukkit.getPlayerExact(name);
        if (online != null) {
            return CompletableFuture.completedFuture(Optional.of(new PlayerRef(online.getUniqueId(), online.getName())));
        }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(name);
        if (cached != null && cached.hasPlayedBefore()) {
            String n = cached.getName() == null ? name : cached.getName();
            return CompletableFuture.completedFuture(Optional.of(new PlayerRef(cached.getUniqueId(), n)));
        }
        return db.query(c -> {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT uuid, name FROM dk_players WHERE name_lower = ? ORDER BY last_seen DESC LIMIT 1")) {
                ps.setString(1, name.toLowerCase(Locale.ROOT));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new PlayerRef(UUID.fromString(rs.getString(1)), rs.getString(2)));
                    }
                }
            }
            return Optional.empty();
        });
    }

    public record PlayerRef(UUID uuid, String name) {}
}
