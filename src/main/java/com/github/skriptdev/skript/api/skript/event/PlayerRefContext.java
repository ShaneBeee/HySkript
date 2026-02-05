package com.github.skriptdev.skript.api.skript.event;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * Represents a {@link TriggerContext} which includes a {@link PlayerRef}
 */
public interface PlayerRefContext extends TriggerContext {

    PlayerRef getPlayerRef();

}
