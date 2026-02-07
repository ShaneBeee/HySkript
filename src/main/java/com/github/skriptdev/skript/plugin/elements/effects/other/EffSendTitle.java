package com.github.skriptdev.skript.plugin.elements.effects.other;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.EventTitleUtil;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffSendTitle extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffSendTitle.class, "send [:major] title %string/message% to %players/playerrefs/worlds%",
                "send [:major] title %string/message% with (subtitle|secondary title) %string/message% to %players/playerrefs/worlds%")
            .name("Send Title")
            .description("Sends a title to players/worlds with an optional subtitle.",
                    "The title can be major or minor, depending on the syntax used.",
                "Not sure what `major` is for, it doesn't seem to do anything different.")
            .examples("send major title \"Hello World!\" to player")
            .since("1.0.0")
            .register();

    }

    private boolean major;
    private Expression<?> title;
    private Expression<?> subtitle;
    private Expression<?> receivers;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.major = parseContext.hasMark("major");
        if (matchedPattern == 0) {
            this.title = expressions[0];
            this.receivers = expressions[1];
        } else if (matchedPattern == 1) {
            this.title = expressions[0];
            this.subtitle = expressions[1];
            this.receivers = expressions[2];
        }

        return true;
    }

    @SuppressWarnings("removal")
    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        Object messageObject = this.title.getSingle(ctx).orElse(null);
        if (messageObject == null) {
            Utils.error("Cannot send title without a message.");
            return;
        }

        Message title;
        if (messageObject instanceof String s) {
            title = Message.raw(s);
        } else if (messageObject instanceof Message m) {
            title = m;
        } else {
            Utils.error("Cannot send title with invalid message type: %s", messageObject.getClass().getSimpleName());
            return;
        }

        Message subtitle = Message.empty();
        if (this.subtitle != null) {
            Object o = this.subtitle.getSingle(ctx).orElse(null);
            if (o instanceof String s) {
                subtitle = Message.raw(s);
            } else if (o instanceof Message m) {
                subtitle = m;
            }
        }

        // Defaults from the game
        float duration = 4.0f;
        float fadein = 1.5f;
        float fadeout = 1.5f;

        for (Object object : this.receivers.getArray(ctx)) {
            if (object instanceof Player player) {
                EventTitleUtil.showEventTitleToPlayer(player.getPlayerRef(), title, subtitle, this.major);
            } else if (object instanceof PlayerRef playerRef) {
                EventTitleUtil.showEventTitleToPlayer(playerRef, title, subtitle, this.major);
            } else if (object instanceof World world) {
                Store<EntityStore> store = world.getEntityStore().getStore();
                EventTitleUtil.showEventTitleToWorld(title, subtitle, this.major, null, duration, fadein, fadeout, store);
            }
        }
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String sub = this.subtitle != null ? "with subtitle " + this.subtitle.toString(ctx, debug) : "";
        return String.format("send %s title %s %s to %s",
            this.major ? "major" : "minor",
            this.title.toString(ctx, debug),
            sub,
            this.receivers.toString(ctx, debug));
    }

}
