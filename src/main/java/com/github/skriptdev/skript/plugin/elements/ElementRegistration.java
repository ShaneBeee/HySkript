package com.github.skriptdev.skript.plugin.elements;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.github.skriptdev.skript.api.utils.Utils;
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
import io.github.syst3ms.skriptparser.lang.Structure;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
import io.github.syst3ms.skriptparser.structures.functions.Functions;

public class ElementRegistration {

    private final SkriptRegistration registration;

    public ElementRegistration(SkriptRegistration registration) {
        this.registration = registration;
    }

    public void registerElements() {
        Utils.log("Loading HySkript elements...");

        // INITIALIZE BASE SKRIPT-PARSER
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
        DefaultFunctions.register(this.registration);

        // COMMAND
        ScriptCommand.register(this.registration);
        ScriptSubCommand.register(this.registration);

        // FINALIZE SETUP
        this.registration.register();

        printSyntaxCount();
    }

    private void printSyntaxCount() {
        var mainRegistration = Parser.getMainRegistration();

        int structureSize = 0;
        int eventSize = 0;
        for (SkriptEventInfo<?> event : this.registration.getEvents()) {
            if (Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                structureSize++;
            } else {
                eventSize++;
            }
        }
        for (SkriptEventInfo<?> event : mainRegistration.getEvents()) {
            if (Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                structureSize++;
            } else {
                eventSize++;
            }
        }
        int effectSize = this.registration.getEffects().size() + mainRegistration.getEffects().size();
        int expsSize = this.registration.getExpressions().size() + mainRegistration.getExpressions().size();
        int secSize = this.registration.getSections().size() + mainRegistration.getSections().size();
        int typeSize = this.registration.getTypes().size() + mainRegistration.getTypes().size();
        int funcSize = Functions.getAllFunctions().size();

        int total = structureSize + eventSize + effectSize + expsSize + secSize + typeSize + funcSize;

        Utils.log("Loaded %s HySkript elements:", total);
        Utils.log("- Types: %s", typeSize);
        Utils.log("- Structures: %s", structureSize);
        Utils.log("- Events: %s ", eventSize);
        Utils.log("- Effects: %s", effectSize);
        Utils.log("- Expressions: %s", expsSize);
        Utils.log("- Sections: %s", secSize);
        Utils.log("- Functions: %s", funcSize);
    }

}
