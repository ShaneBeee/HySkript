package com.github.skriptdev.skript.plugin.elements.effects.entity;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class EffEntityAttitude extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffEntityAttitude.class,
                "make %npcentities% ignore %entities% [for %duration%]",
                "make %npcentities% hostile towards %entities% [for %duration%]",
                "make %npcentities% neutral to %entities% [for %duration%]",
                "make %npcentities% friendly to %entities% [for %duration%]",
                "make %npcentities% revered towards %entities% [for %duration%]")
            .name("Entity Attitude")
            .description("Temporarily change the attitude of NPC entities to other entities.",
                "If using the optional duration, this will last then revert back to its original attitude.",
                "**NOTE**: This is not persistent.")
            .examples("make target entity of player friendly to player for 10 minutes")
            .since("INSERT VERSION")
            .register();
    }

    private int pattern;
    private Expression<NPCEntity> npcEntities;
    private Expression<Entity> targets;
    private Expression<Duration> duration;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.pattern = matchedPattern;
        this.npcEntities = (Expression<NPCEntity>) expressions[0];
        this.targets = (Expression<Entity>) expressions[1];
        if (expressions.length == 3) {
            this.duration = (Expression<Duration>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        double durationSeconds = Double.MAX_VALUE;
        if (this.duration != null) {
            Duration duration = this.duration.getSingle(ctx).orElse(null);
            if (duration == null) return;
            durationSeconds = (double) duration.toMillis() / 1000;
        }

        Attitude attitude = Attitude.values()[this.pattern];



        for (NPCEntity npcEntity : this.npcEntities.getArray(ctx)) {
            Role npcRole = npcEntity.getRole();
            if (npcRole == null) continue;

            WorldSupport worldSupport = npcRole.getWorldSupport();
            Ref<EntityStore> npcRef = npcEntity.getReference();
            if (npcRef == null) continue;

            for (Entity target : this.targets.getArray(ctx)) {
                Ref<EntityStore> targetRef = target.getReference();
                if (targetRef == null) continue;

                // Clear current targetted entities
                EntityUtils.clearMarkedEntity(npcEntity, target);

                // Override attitude
                worldSupport.overrideAttitude(targetRef, attitude, durationSeconds);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String duration = this.duration != null ? " for " + this.duration.toString(ctx, debug) : "";
        String type = switch (this.pattern) {
            case 0 -> "ignore";
            case 1 -> "hostile towards";
            case 2 -> "neutral to";
            case 3 -> "friendly to";
            case 4 -> "revered towards";
            default -> "unknown";
        };
        return "make " + this.npcEntities.toString(ctx, debug) + " " + type + " "
            + this.targets.toString(ctx, debug) + duration;
    }

}
