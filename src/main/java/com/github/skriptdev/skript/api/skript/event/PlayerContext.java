package com.github.skriptdev.skript.api.skript.event;

import com.hypixel.hytale.server.core.entity.entities.Player;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * Represents a {@link TriggerContext} which includes a {@link Player}
 */
public interface PlayerContext extends TriggerContext {

    Player getPlayer();

}
