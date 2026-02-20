package com.github.skriptdev.skript.plugin.elements.effects.world;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.variables.VariableMap;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class EffChunkLoadAsync extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffChunkLoadAsync.class, "async load chunk at %location% [load:and keep loaded]")
            .name("Async Load Chunk")
            .description("Asynchronously loads the chunk at the specified location.",
                "The code after this effect will run when the chunk is loaded.",
                "This will not freeze the thread the effect is executed on.",
                "The additional `keep loaded` will keep the chunk loaded in memory until the chunk is unloaded.")
            .examples("async load chunk at {_loc}",
                "async load chunk at {_loc} and keep loaded",
                "set blocktype of block at {_loc} to rock_stone")
            .since("INSERT VERSION")
            .register();
    }

    private boolean keepLoaded;
    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.keepLoaded = parseContext.hasMark("load");
        this.location = (Expression<Location>) expressions[0];
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();
        Location loc = this.location.getSingle(ctx).orElse(null);
        if (loc == null) return nextStatement;

        World world = Universe.get().getWorld(loc.getWorld());
        if (world == null) return nextStatement;

        Vector3i pos = loc.getPosition().toVector3i();
        int x = pos.getX();
        int z = pos.getZ();

        // Save variables for later
        VariableMap variableMap = Variables.copyLocalVariables(ctx);

        Runnable worldRun = () -> {
            world.getChunkAsync(ChunkUtil.indexChunkFromBlock(x, z))
                .thenAccept(chunk -> {
                    if (this.keepLoaded) {
                        chunk.addKeepLoaded();
                    }
                    nextStatement.ifPresent(statement -> {
                        // Pass local variables back after the chunk has loaded
                        Variables.setLocalVariables(ctx, variableMap);

                        // Run all statements
                        Statement.runAll(statement, ctx);

                        // Clear locals
                        Variables.clearLocalVariables(ctx);
                    });
                });
        };

        // Make sure we run in the correct thread
        if (world.isInThread()) {
            worldRun.run();
        } else {
            world.execute(worldRun);
        }
        return Optional.empty();
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        // Walk... don't run, I mean... execute!
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String keepLoaded = this.keepLoaded ? " and keep loaded" : "";
        return "async load chunk at " + this.location.toString(ctx, debug) + keepLoaded;
    }

}
