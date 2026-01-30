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
import org.jetbrains.annotations.NotNull;

public class ExprBlockType implements Expression<BlockType> {

    public static void register(SkriptRegistration registration) {
        registration.newExpression(ExprBlockType.class, BlockType.class, true,
                "block[ ]type at %vector3i% in %world%",
                "block[ ]type at %location%")
            .name("Block Type")
            .description("Returns the BlockType at a given location in a world.")
            .examples("set {_block} to blocktype at location of player",
                "set {_block} to blocktype at vector3i(1, 2, 3) in world of player",
                "if blocktype at vector3i(1,1,1) = rock_stone_brick:")
            .since("INSERT VERSION")
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
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        if (this.loc != null) {
            return "blocktype at " + this.loc.toString(ctx, debug);
        } else {
            return "blocktype at " + this.pos.toString(ctx, debug) + " in " + this.world.toString(ctx, debug);
        }
    }

}
