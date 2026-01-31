package com.github.skriptdev.skript.plugin.elements.expressions.block;

import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprBlockTypeAtLocation implements Expression<BlockType> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprBlockTypeAtLocation.class, BlockType.class, true,
                "block[ ]type at %vector3i% in %world%",
                "block[ ]type at %location%")
            .name("Block Type at Location")
            .description("Get/set the BlockType at a given location in a world.")
            .examples("set {_block} to blocktype at location of player",
                "set {_block} to blocktype at vector3i(1, 2, 3) in world of player",
                "set blocktype at location of player to empty # This is how Hytale refers to air",
                "if blocktype at vector3i(1,1,1) = rock_stone_brick:")
            .since("1.0.0")
            .register();
    }

    private Expression<Vector3i> pos;
    private Expression<World> world;
    private Expression<Location> loc;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        if (matchedPattern == 0) {
            this.pos = (Expression<Vector3i>) expressions[0];
            this.world = (Expression<World>) expressions[1];
        } else {
            this.loc = (Expression<Location>) expressions[0];
        }
        return true;
    }

    @Override
    public BlockType[] getValues(@NotNull TriggerContext ctx) {
        if (this.pos != null && this.world != null) {
            World world = this.world.getSingle(ctx).orElse(null);
            Vector3i vector3i = this.pos.getSingle(ctx).orElse(null);
            if (world != null && vector3i != null)
                return new BlockType[]{world.getBlockType(vector3i)};
        } else if (this.loc != null) {
            Location location = this.loc.getSingle(ctx).orElse(null);
            if (location != null) {
                World world = Universe.get().getWorld(location.getWorld());
                Vector3i vector3i = location.getPosition().toVector3i();
                if (world != null)
                    return new BlockType[]{world.getBlockType(vector3i)};
            }
        }
        return null;
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{BlockType.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeMode != ChangeMode.SET) return;
        // Even though changeWith is NotNull... it still can be null
        if (changeWith == null) return;

        if (!(changeWith[0] instanceof BlockType type)) return;

        if (this.loc != null) {
            Location location = this.loc.getSingle(ctx).orElse(null);
            if (location == null) return;
            World world = Universe.get().getWorld(location.getWorld());
            Vector3i vector3i = location.getPosition().toVector3i();
            if (world != null)
                world.setBlock(vector3i.getX(), vector3i.getY(), vector3i.getZ(), type.getId());
        } else if (this.pos != null && this.world != null) {
            Vector3i pos = this.pos.getSingle(ctx).orElse(null);
            World world = this.world.getSingle(ctx).orElse(null);
            if (pos == null || world == null) return;

            world.setBlock(pos.getX(), pos.getY(), pos.getZ(), type.getId());
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.loc != null) {
            return "blocktype at " + this.loc.toString(ctx, debug);
        } else {
            return "blocktype at " + this.pos.toString(ctx, debug) + " in " + this.world.toString(ctx, debug);
        }
    }

}
