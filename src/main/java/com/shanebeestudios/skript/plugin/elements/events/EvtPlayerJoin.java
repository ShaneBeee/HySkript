package com.shanebeestudios.skript.plugin.elements.events;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.shanebeestudios.skript.api.skript.eventcontext.PlayerEventContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.Nullable;

public class EvtPlayerJoin extends SkriptEvent {

    public static void register(SkriptRegistration registration) {
        registration.newEvent(EvtPlayerJoin.class, "player connect", "player ready", "player quit")
            .setHandledContexts(PlayerEventContext.class)
            .register();

        registration.addContextValue(PlayerEventContext.class, Player.class,
            true, "player", PlayerEventContext::getPlayer);
    }

    private int pattern;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, ParseContext parseContext) {
        this.pattern = matchedPattern;
        return true;
    }

    @Override
    public boolean check(TriggerContext ctx) {
        if (!(ctx instanceof PlayerEventContext playerEventContext)) return false;
        if (this.pattern != playerEventContext.getPattern()) return false;
        return true;
    }

    public int getPattern() {
        return this.pattern;
    }

    @Override
    public String toString(@Nullable TriggerContext ctx, boolean debug) {
        return "";
    }

}
