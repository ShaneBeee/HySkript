package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.github.skriptdev.skript.api.hytale.utils.AssetStoreUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExprEnvironmentAtLocation implements Expression<Environment> {

    public static void register(SkriptRegistration reg) {
        reg.newExpression(ExprEnvironmentAtLocation.class, Environment.class, true,
                "environment at %location%")
            .name("Environment at Location")
            .description("Get/set the environment at a location.")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Location> location;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.location = (Expression<Location>) expressions[0];
        return true;
    }

    @Override
    public Environment[] getValues(@NotNull TriggerContext ctx) {
        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) return null;

        BlockChunk blockChunk = getBlockChunk(location);
        if (blockChunk == null) return null;

        Vector3i position = location.getPosition().toVector3i();
        int id = blockChunk.getEnvironment(position);
        return new Environment[]{AssetStoreUtils.getEnvironment(id)};
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET) return Optional.of(new Class<?>[]{Environment.class});
        return Optional.empty();
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        if (changeWith == null || changeWith.length == 0 || !(changeWith[0] instanceof Environment env)) return;

        Location location = this.location.getSingle(ctx).orElse(null);
        if (location == null) return;

        BlockChunk blockChunk = getBlockChunk(location);
        if (blockChunk == null) return;

        int index = AssetStoreUtils.getEnvironmentIndex(env);
        Vector3i position = location.getPosition().toVector3i();

        int x = position.getX();
        int y = position.getY();
        int z = position.getZ();

        blockChunk.setEnvironment(x, y, z, index);

        World world = Universe.get().getWorld(location.getWorld());
        assert world != null;

        long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
        world.getNotificationHandler().updateChunk(chunkIndex);
    }

    private BlockChunk getBlockChunk(Location location) {
        String worldName = location.getWorld();
        World world = Universe.get().getWorld(worldName);
        if (world == null) return null;

        Vector3i position = location.getPosition().toVector3i();

        WorldChunk chunk = world.getChunk(ChunkUtil.indexChunkFromBlock(position.getX(), position.getZ()));
        if (chunk == null) return null;

        return chunk.getBlockChunk();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "environment at " + this.location.toString(ctx, debug);
    }

}
