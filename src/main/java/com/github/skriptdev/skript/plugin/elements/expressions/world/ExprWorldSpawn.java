package com.github.skriptdev.skript.plugin.elements.expressions.world;

import com.hypixel.hytale.math.vector.Location;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.spawn.GlobalSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.changers.ChangeMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class ExprWorldSpawn extends PropertyExpression<World, Location> {

    private static final UUID RANDOM_UUID = UUID.randomUUID();

    public static void register(SkriptRegistration registration) {
        registration.newPropertyExpression(ExprWorldSpawn.class, Location.class,
                "world spawn [location]", "worlds")
            .name("World Spawn Location")
            .description("Get/set the spawn location of a world.",
                "When setting you can use a location, vector3i, vector3d, or a vector3f.")
            .examples("teleport all players to world spawn of world of player",
                "set world spawn of world of player to location of player")
            .since("1.0.0")
            .register();
    }

    @Override
    public @Nullable Location getProperty(World world) {
        ISpawnProvider spawnProvider = world.getWorldConfig().getSpawnProvider();
        if (spawnProvider == null) return null;

        Transform spawnPoint = spawnProvider.getSpawnPoint(world, RANDOM_UUID);
        return new Location(world.getName(), spawnPoint);
    }

    @Override
    public Optional<Class<?>[]> acceptsChange(@NotNull ChangeMode mode) {
        if (mode == ChangeMode.SET)
            return Optional.of(new Class<?>[]{Location.class, Vector3i.class, Vector3d.class, Vector3f.class});
        return Optional.empty();
    }

    @Override
    public void change(@NotNull TriggerContext ctx, @NotNull ChangeMode changeMode, Object @NotNull [] changeWith) {
        Optional<? extends World> single = getOwner().getSingle(ctx);
        if (single.isEmpty()) return;

        World world = single.get();
        Object object = changeWith[0];
        Transform transform = switch (object) {
            case Location location -> location.toTransform();
            case Vector3i vector3i -> new Transform(vector3i.toVector3d(), Vector3f.ZERO);
            case Vector3d vector3d -> new Transform(vector3d, Vector3f.ZERO);
            case Vector3f vector3f -> new Transform(vector3f.toVector3d(), Vector3f.ZERO);
            case null, default -> null;
        };
        if (transform == null) return;

        WorldConfig worldConfig = world.getWorldConfig();
        worldConfig.setSpawnProvider(new GlobalSpawnProvider(transform));
        worldConfig.markChanged();
    }

}
