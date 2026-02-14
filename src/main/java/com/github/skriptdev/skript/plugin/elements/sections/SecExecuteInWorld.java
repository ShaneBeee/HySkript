package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.variables.Variables;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SecExecuteInWorld extends CodeSection {

    public static void register(SkriptRegistration registration) {
        registration.newSection(SecExecuteInWorld.class, "execute in [world] %world%")
            .name("Execute In World")
            .description("Executes the code inside the section in the specified world.")
            .examples("execute in world of player:",
                "\tkill player")
            .since("1.0.0")
            .register();
    }

    private Expression<World> world;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.world = (Expression<World>) expressions[0];
        parseContext.getParserState().setDelayed(true);
        return true;
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> first = getFirst();
        if (first.isPresent()) {
            Optional<? extends World> single = this.world.getSingle(ctx);

            // Copy variables from the time this code executes
            TriggerContext dummy = TriggerContext.DUMMY;
            Variables.copyLocalVariables(ctx, dummy);

            Statement firstStatement = first.get();
            single.ifPresent(world -> world.execute(() -> {
                // Place the variables back into the original context
                // This is done in case World#execute is delayed
                Variables.copyLocalVariables(dummy, ctx);
                Statement.runAll(firstStatement, ctx);

                // Clear out old variables
                Variables.clearLocalVariables(dummy);
                Variables.clearLocalVariables(ctx);
            }));
        }
        return getNext();
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "execute in world " + this.world.toString(ctx, debug);
    }

}
