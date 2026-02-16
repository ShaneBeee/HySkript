package com.github.skriptdev.skript.api.skript.event;

import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * Represents a {@link TriggerContext} which includes a {@link World}
 */
public interface WorldContext extends TriggerContext {

    World getWorld();

}
