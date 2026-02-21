package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import io.github.syst3ms.skriptparser.file.FileSection;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.lang.entries.SectionConfiguration;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Optional;

public class SecSendTitle extends CodeSection {

    public static void register(SkriptRegistration reg) {
        reg.newSection(SecSendTitle.class, "send title to %players/playerrefs/worlds%")
            .name("Send Title")
            .description("Sends a title to players/worlds.",
                "This is a section with a few different entries that can be used to customize the title.",
                "**Entries**:",
                " - `title` = The title to send (required: String/Message)",
                " - `secondary_title` = The smaller message above [optional: String/Message]",
                " - `major` = Whether to use the nicer border [optional: Boolean]",
                " - `icon` = The icon to show [optional: String]",
                " - `fade_in` = The time it takes the title to fade in [optional: Duration]",
                " - `fade_out` = The time it takes the title to fade out [optional: Duration]",
                " - `duration` = The time the title will stay on the screen [optional: Duration]")
            .examples("send title to player:",
                "\ttitle: \"Imma Cool Title\"",
                "\tsecondary_title: \"Imma Little Secodary\"",
                "\tmajor: true",
                "\tfade_in: 1 tick",
                "\tfade_out: 1 tick",
                "\tduration: 1 minute")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<?> receivers;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.receivers = expressions[0];
        return true;
    }

    SectionConfiguration config = new SectionConfiguration.Builder()
        .addExpression("title", Object.class, false)
        .addOptionalExpression("secondary_title", Object.class, false)
        .addOptionalLiteral("major", Boolean.class)
        .addOptionalExpression("icon", String.class, false)
        .addOptionalExpression("duration", Duration.class, false)
        .addOptionalExpression("fade_in", Duration.class, false)
        .addOptionalExpression("fade_out", Duration.class, false)
        .build();

    @Override
    public boolean loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        return this.config.loadConfiguration(null, section, parserState, logger);
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();


        Expression<Object> titleExpr = this.config.getExpression("title", Object.class).orElse(null);
        if (titleExpr == null) return nextStatement;

        Optional<?> titleSingle = titleExpr.getSingle(ctx);
        if (titleSingle.isEmpty()) return nextStatement;

        Message title;
        Object o = titleSingle.get();
        if (o instanceof Message message) {
            title = message;
        } else if (o instanceof String s) {
            title = Message.raw(s);
        } else {
            return nextStatement;
        }

        Message secondaryTitle = Message.empty();
        Expression<Object> objectExpression = this.config.getExpression("secondary_title", Object.class).orElse(null);
        if (objectExpression != null) {
            Optional<?> secondaryTitleSingle = objectExpression.getSingle(ctx);
            if (secondaryTitleSingle.isPresent()) {

                Object o1 = secondaryTitleSingle.get();
                if (o1 instanceof Message message) {
                    secondaryTitle = message;
                } else if (o1 instanceof String s) {
                    secondaryTitle = Message.raw(s);
                }
            }
        }

        Optional<Boolean> majorOptional = this.config.getValue("major", Boolean.class);
        boolean major = majorOptional.orElse(false);

        Expression<String> iconExpr = this.config.getExpression("icon", String.class).orElse(null);
        String icon = null;
        if (iconExpr != null) {
            Optional<? extends String> iconSingle = iconExpr.getSingle(ctx);
            if (iconSingle.isPresent()) {
                icon = iconSingle.get();
            }
        }

        float duration = 4.0f;
        Expression<Duration> durationExpr = this.config.getExpression("duration", Duration.class).orElse(null);
        if (durationExpr != null) {
            Optional<? extends Duration> single = durationExpr.getSingle(ctx);
            if (single.isPresent()) {
                Duration duration1 = single.get();
                duration = duration1.toMillis() / 1000.0f;
            }
        }

        float fadein = 1.5f;
        Expression<Duration> fadeinExpr = this.config.getExpression("fade_in", Duration.class).orElse(null);
        if (fadeinExpr != null) {
            Optional<? extends Duration> single = fadeinExpr.getSingle(ctx);
            if (single.isPresent()) {
                Duration duration1 = single.get();
                fadein = duration1.toMillis() / 1000.0f;
            }
        }

        float fadeout = 1.5f;
        Expression<Duration> fadeoutExpr = this.config.getExpression("fade_out", Duration.class).orElse(null);
        if (fadeoutExpr != null) {
            Optional<? extends Duration> single = fadeoutExpr.getSingle(ctx);
            if (single.isPresent()) {
                Duration duration1 = single.get();
                fadeout = duration1.toMillis() / 1000.0f;
            }
        }

        for (Object object : this.receivers.getArray(ctx)) {
            if (object instanceof Player player) {
                PlayerRef ref = EntityUtils.getComponent(player, PlayerRef.getComponentType());
                if (ref == null) continue;

                EventTitleUtil.showEventTitleToPlayer(ref, title, secondaryTitle, major, icon, duration, fadein, fadeout);
            } else if (object instanceof PlayerRef playerRef) {
                EventTitleUtil.showEventTitleToPlayer(playerRef, title, secondaryTitle, major, icon, duration, fadein, fadeout);
            } else if (object instanceof World world) {
                Store<EntityStore> store = world.getEntityStore().getStore();
                EventTitleUtil.showEventTitleToWorld(title, secondaryTitle, major, null, duration, fadein, fadeout, store);
            }
        }

        return nextStatement;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "send title to " + this.receivers.toString(ctx, debug);
    }

}
