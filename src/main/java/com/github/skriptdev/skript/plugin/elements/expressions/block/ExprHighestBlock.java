package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.github.skriptdev.skript.api.hytale.Block;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class ExprHighestBlock implements Expression<Object> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprHighestBlock.class, Object.class, true,
                "highest block at %location%",
                "highest block y at %location%",
                "highest block location at %location%")
            .name("Highest Block")
            .description("Get the highest block/y coord/location at a location.",
                "This represents the highest solid block generated at a location.")
            .examples("teleport player to highest block location at location of player")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.location = (Expression<Location>) expressions[0];
        return true;
    }

    @Override
    public Object[] getValues(@NotNull TriggerContext ctx) {
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) return null;

        String worldName = location.getWorld();
        World world = Universe.get().getWorld(worldName);
        if (world == null) return null;

        Vector3i pos = location.getPosition().toVector3i().clone();
        int x = pos.getX();
        int z = pos.getZ();
        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
        if (chunk == null) return null;

        short height = chunk.getHeight(x, z);
        pos.setY(height);
        if (this.pattern == 1) {
            return new Number[]{height};
        } else if (this.pattern == 2) {
            return new Location[]{new Location(worldName, pos)};
        } else {
            return new Block[]{new Block(world, pos)};
        }
    }

    @Override
    public Class<?> getReturnType() {
        if (this.pattern == 0) return Block.class;
        return Number.class;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = switch (this.pattern) {
            case 1 -> "block y";
            case 2 -> "block location";
            default -> "block";
        };
        return "highest " + type + " at " + this.location.toString(ctx, debug);
    }

}
