package com.github.skriptdev.skript.plugin.elements;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.plugin.elements.command.ScriptCommand;
import com.github.skriptdev.skript.plugin.elements.command.ScriptSubCommand;
import com.github.skriptdev.skript.plugin.elements.types.DefaultComparators;
import com.github.skriptdev.skript.plugin.elements.types.DefaultConverters;
import com.github.skriptdev.skript.plugin.elements.conditions.ConditionHandler;
import com.github.skriptdev.skript.plugin.elements.effects.EffectHandler;
import com.github.skriptdev.skript.plugin.elements.events.EventHandler;
import com.github.skriptdev.skript.plugin.elements.expressions.ExpressionHandler;
import com.github.skriptdev.skript.plugin.elements.functions.DefaultFunctions;
import com.github.skriptdev.skript.plugin.elements.sections.SectionHandler;
import com.github.skriptdev.skript.plugin.elements.types.Types;
import io.github.syst3ms.skriptparser.Parser;

public class ElementRegistration {

    private final SkriptRegistration registration;

    public ElementRegistration(SkriptRegistration registration) {
        this.registration = registration;
    }

    public void registerElements() {
        Parser.init(new String[0], new String[0], new String[0], true);

        // TYPES
        Types.register(this.registration);

        // COMPARATORS/CONVERTERS
        DefaultComparators.register();
        DefaultConverters.register();

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

        // FUNCTIONS
        DefaultFunctions.register();

        // COMMAND
        ScriptCommand.register(this.registration);
        ScriptSubCommand.register(this.registration);
    }

}
