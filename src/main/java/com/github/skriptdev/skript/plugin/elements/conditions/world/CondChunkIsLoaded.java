package com.github.skriptdev.skript.plugin.elements.conditions.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class CondChunkIsLoaded extends ConditionalExpression {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(CondChunkIsLoaded.class, Boolean.class, true,
                "chunk at %location% is loaded",
                "chunk at %location% (is not|isn't) loaded")
            .name("Chunk is Loaded")
            .description("Check if a chunk is loaded at a location.")
            .examples("if chunk at {_loc} is loaded:",
                "\tteleport player to {_loc}")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.location = (Expression<Location>) expressions[0];
        setNegated(matchedPattern == 1);
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return this.location.check(ctx, location -> {
            World world = Universe.get().getWorld(location.getWorld());
            if (world != null) {
                Vector3i pos = location.getPosition().toVector3i();
                int x = pos.getX();
                int z = pos.getZ();
                long index = ChunkUtil.indexChunkFromBlock(x, z);
                Ref<ChunkStore> chunkReference = world.getChunkStore().getChunkReference(index);
                if (isNegated()) {
                    return chunkReference == null;
                }
                return chunkReference != null;
            }
            return false;
        });
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String type = isNegated() ? "is not" : "is";
        return "chunk at " + this.location.toString(ctx, debug) + " " + type + " loaded";
    }

}
