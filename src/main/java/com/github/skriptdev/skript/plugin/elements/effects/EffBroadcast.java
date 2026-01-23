package com.github.skriptdev.skript.plugin.elements.effects;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class EffBroadcast extends Effect {

    public static void register(SkriptRegistration registration) {
        registration.newEffect(EffBroadcast.class,
                "broadcast %strings% [(to|in) %worlds%]")
            .name("Broadcast")
            .description("Broadcasts a message to all players in the server or in a specific world.")
            .examples("broadcast \"HELLO EVERYONE!!!\"",
                "broadcast \"Free Cheese At Spawn\" in world named \"default\"")
            .register();
    }

    private Expression<String> messages;
    private Expression<World> worlds;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.messages = (Expression<String>) expressions[0];
        if (expressions.length > 1) {
            this.worlds = (Expression<World>) expressions[1];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        String[] messages = this.messages.getValues(ctx);

        if (this.worlds != null) {
            for (String message : messages) {
                Message rawMessage = Message.raw(message);
                for (World world : this.worlds.getValues(ctx)) {
                    world.sendMessage(rawMessage);
                }
            }
        } else {
            for (String message : messages) {
                Universe.get().sendMessage(Message.raw(message));
            }

        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String worlds = this.worlds != null ? " in " + this.worlds.toString(ctx, debug) : "";
        return "broadcast " + this.messages.toString(ctx, debug) + worlds;
    }
}
