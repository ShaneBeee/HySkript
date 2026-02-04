package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDoublePosition;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeIntPosition;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class ExprRelativePositionResolve implements Expression<Location> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprRelativePositionResolve.class, Location.class, true,
                "resolve %relativeposition/relativeblockposition% for %location%")
            .name("Resolve Relative Position")
            .description("Resolves a relative position to a location.")
            .examples("player command /LeTP <relative:relative_position:\"Location to TP to\">:",
                "\ttrigger:",
                "\t\tset {_loc} to resolve {_relative} for location of player",
                "\t\tteleport player to {_loc}",
                "# Typing '/letp ~ ~10 ~' would teleport you 10 blocks above your location")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> relativePosition;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.relativePosition = expressions[0];
        this.location = (Expression<Location>) expressions[1];
        return true;
    }

    @Override
    public Location[] getValues(@NotNull TriggerContext ctx) {
        Object o = this.relativePosition.getSingle(ctx).orElse(null);
        Location location = this.location.getSingle(ctx).orElse(null);
        if (o == null || location == null) return null;

        World world = Universe.get().getWorld(location.getWorld());
        if (world == null) return null;

        Vector3f rotation = location.getRotation().clone();
        Location relativeLocation;
        if (o instanceof RelativeDoublePosition d) {
            Vector3d pos = d.getRelativePosition(location.getPosition().clone(), world);
            relativeLocation = new Location(location.getWorld(), pos, rotation);
        } else if (o instanceof RelativeIntPosition i) {
            Vector3i pos = i.getBlockPosition(location.getPosition().clone(), world.getChunkStore());
            relativeLocation = new Location(location.getWorld(), pos.toVector3d(), rotation);
        } else {
            return null;
        }

        return new Location[]{relativeLocation};
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "resolve " + this.relativePosition.toString(ctx, debug) + " for " + this.location.toString(ctx, debug);
    }

}
