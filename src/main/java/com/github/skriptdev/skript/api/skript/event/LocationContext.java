package com.github.skriptdev.skript.api.skript.event;

import com.hypixel.hytale.math.vector.Location;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

/**
 * Represents a {@link TriggerContext} which includes a {@link Location}
 */
public interface LocationContext extends TriggerContext {

    Location getLocation();

}
