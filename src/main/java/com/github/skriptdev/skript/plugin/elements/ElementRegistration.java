package com.github.skriptdev.skript.plugin.elements;

import com.github.skriptdev.skript.plugin.Skript;
import com.github.skriptdev.skript.plugin.elements.conditions.ConditionHandler;
import com.github.skriptdev.skript.plugin.elements.effects.EffectHandler;
import com.github.skriptdev.skript.plugin.elements.events.EventHandler;
import com.github.skriptdev.skript.plugin.elements.expressions.ExpressionHandler;
import com.github.skriptdev.skript.plugin.elements.listeners.ListenerHandler;
import com.github.skriptdev.skript.plugin.elements.sections.SectionHandler;
import com.github.skriptdev.skript.plugin.elements.types.Types;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

public class ElementRegistration {

    private final SkriptRegistration registration;
    private final ListenerHandler listenerHandler;

    public ElementRegistration(Skript skript) {
        this.registration = skript.getRegistration();
        this.listenerHandler = new ListenerHandler(skript);
    }

    public void registerElements() {
        Parser.init(new String[0], new String[0], new String[0], true);

        // TYPES
        Types.register(this.registration);

        // CONDITIONS
        ConditionHandler.register(this.registration);

        // EFFECTS
        EffectHandler.register(this.registration);

        // EXPRESSIONS
        ExpressionHandler.register(this.registration);

        // SECTIONS
        SectionHandler.register(this.registration);

        // EVENTS
        EventHandler.register(this.registration);
    }

    public ListenerHandler getListenerHandler() {
        return this.listenerHandler;
    }

    public void clearTriggers() {
        this.listenerHandler.clearTriggers();
    }

    public void handleTrigger(@NotNull Trigger trigger) {
        this.listenerHandler.handleTrigger(trigger);
    }

    public void finishedLoading() {
        this.listenerHandler.finishedLoading();
    }

}
