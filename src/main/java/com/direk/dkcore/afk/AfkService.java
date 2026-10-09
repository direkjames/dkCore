package com.direk.dkcore.afk;

import java.util.UUID;

/**
 * Anything that knows whether a player is AFK.
 * <p>
 * DirekAntiAFK will register this once it migrates to dkCore:
 * <pre>
 * Bukkit.getServicesManager().register(AfkService.class, uuid -> afkManager.isAfk(uuid),
 *         this, ServicePriority.Normal);
 * </pre>
 * and call {@link DkAfkChangeEvent} whenever someone goes AFK or comes back.
 * Other plugins just call {@code DkCore.get().isAfk(uuid)}.
 * <p>
 * Author: direk james
 */
@FunctionalInterface
public interface AfkService {
    boolean isAfk(UUID uuid);
}
