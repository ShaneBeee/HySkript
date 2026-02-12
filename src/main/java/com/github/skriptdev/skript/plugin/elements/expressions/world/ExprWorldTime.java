package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.util.SkriptDate;
import io.github.syst3ms.skriptparser.util.Time;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.TimeZone;

public class ExprWorldTime implements Expression<Object> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprWorldTime.class, Object.class, true,
                "world (:time|date) (in|of) %world%")
            .name("World Date/Time")
            .description("Gets the date or time of a world.")
            .examples("set {_date} to world date of world of player",
                "set {_time} to world time of world of player")
            .since("INSERT VERSION")
            .register();
    }

    private boolean time;
    private Expression<World> world;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.world = (Expression<World>) expressions[0];
        this.time = parseContext.hasMark("time");
        return true;
    }

    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        World world = this.world.getSingle(ctx).orElse(null);
        if (world == null) return null;

        WorldTimeResource worldTimeResource = world.getEntityStore().getStore().getResource(WorldTimeResource.getResourceType());
        Instant gameTime = worldTimeResource.getGameTime();

        LocalDateTime gameDateTime = worldTimeResource.getGameDateTime();

        if (this.time) {
            return new Time[]{Time.of(gameDateTime.toLocalTime())};
        } else {
            SkriptDate skriptDate = SkriptDate.of(gameTime.toEpochMilli(), TimeZone.getTimeZone(ZoneId.systemDefault()));
            return new SkriptDate[]{skriptDate};
        }
    }

    @Override
    public Class<?> getReturnType() {
        if (this.time) return Time.class;
        return SkriptDate.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = this.time ? "time" : "date";
        return "world " + type + " of " + this.world.toString(ctx, debug);
    }

}
