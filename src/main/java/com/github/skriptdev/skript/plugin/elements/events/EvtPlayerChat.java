package com.github.skriptdev.skript.plugin.elements.events;

import com.github.skriptdev.skript.api.skript.eventcontext.CancellableContext;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import org.jetbrains.annotations.NotNull;

public class EvtPlayerChat extends SkriptEvent {

    public static class PlayerChatEventContext implements CancellableContext {

        private boolean cancelled = false;
        private String message;
        private boolean messageChanged = false;

        public PlayerChatEventContext(String message) {
            this.message = message;
        }

        public String[] getMessage() {
            return new String[]{this.message};
        }

        public void setMessage(String message) {
            this.messageChanged = true;
            this.message = message;
        }

        public boolean isMessageChanged() {
            return this.messageChanged;
        }

        @Override
        public String getName() {
            return "player chat";
        }

        @Override
        public boolean isCancelled() {
            return this.cancelled;
        }

        @Override
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    public static void register(SkriptRegistration registration) {
        registration.newEvent(EvtPlayerChat.class, "player chat")
            .setHandledContexts(PlayerChatEventContext.class)
            .name("Player Chat")
            .description("Event triggered when a player sends a message in chat.")
            .since("INSERT VERSION")
            .register();

        registration.newContextValue(PlayerChatEventContext.class,
                String.class,
                true,
                "message",
                PlayerChatEventContext::getMessage)
            .setUsage(ContextValue.Usage.EXPRESSION_OR_ALONE)
            .register();
//        registration.newContextValue(PlayerChatEventContext.class, TODO figure out how to get a player
//            Player.class,
//            true,
//            "player",
//            PlayerChatEventContext::getPlayer)
//            .setUsage(ContextValue.Usage.EXPRESSION_OR_ALONE)
//            .register();
    }

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        return true;
    }

    @Override
    public boolean check(@NotNull TriggerContext ctx) {
        return ctx instanceof PlayerChatEventContext;
    }


    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "player chat";
    }

}
