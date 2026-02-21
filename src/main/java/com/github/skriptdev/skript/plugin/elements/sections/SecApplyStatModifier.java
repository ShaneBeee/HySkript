package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.hytale.utils.EntityUtils;
import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.modules.entitystats.EntityStatMap;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.Modifier.ModifierTarget;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier;
import com.hypixel.hytale.server.core.modules.entitystats.modifier.StaticModifier.CalculationType;
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

import java.util.Optional;

public class SecApplyStatModifier extends CodeSection {

    public static void register(SkriptRegistration reg) {
        reg.newSection(SecApplyStatModifier.class,
                "apply entity stat modifier to %entities%")
            .name("Entity Stat Modifier - Apply")
            .description("Applies a static modifier to the specified entities.",
                "**Entries**:",
                " - `stat` = The EntityStatType to modify (required).",
                " - `key` = The string key used to distinguish your modifier from others (required).",
                " - `value` = The numeric value to modify (required).",
                " - `target` = Whether to target the min/max of the stat. " +
                    "[optional: use either `min` or `max`, default = max].",
                " - `type` = Whether to use additive or multiplicative calculation. " +
                    "[optional: use either `additive` or `multiplicative`, default = additive].")
            .examples("apply entity stat modifier to player:",
                "\tstat: health",
                "\tkey: \"MyKey\"",
                "\tvalue: 5",
                "\ttarget: max",
                "\ttype: multiplicative")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<Entity> entities;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.entities = (Expression<Entity>) expressions[0];
        return true;
    }

    SectionConfiguration config = new SectionConfiguration.Builder()
        .addExpression("stat", EntityStatType.class, false)
        .addExpression("key", String.class, false)
        .addExpression("value", Number.class, false)
        .addOptionalKey("target")
        .addOptionalKey("type")
        .build();

    @Override
    public boolean loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        return this.config.loadConfiguration(null, section, parserState, logger);
    }

    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();
        Expression<EntityStatType> statExpr = this.config.getExpression("stat", EntityStatType.class).orElse(null);
        if (statExpr == null) return nextStatement;

        Optional<? extends EntityStatType> statSingle = statExpr.getSingle(ctx);
        if (statSingle.isEmpty()) return nextStatement;

        EntityStatType stat = statSingle.get();
        int statIndex = EntityStatType.getAssetMap().getIndex(stat.getId());

        Expression<String> keyExpr = this.config.getExpression("key", String.class).orElse(null);
        if (keyExpr == null) return nextStatement;

        Optional<? extends String> keyOptional = keyExpr.getSingle(ctx);
        if (keyOptional.isEmpty()) return nextStatement;

        String key = keyOptional.get();

        ModifierTarget target = ModifierTarget.MAX;
        Object targetKey = this.config.getValue("target");
        if (targetKey != null && targetKey.equals("min")) {
            target = ModifierTarget.MIN;
        }

        CalculationType type = CalculationType.ADDITIVE;
        Object calcType = this.config.getValue("type");
        if (calcType != null && calcType.equals("multiplicative")) {
            type = CalculationType.MULTIPLICATIVE;
        }

        float amount = 0;
        Optional<Expression<Number>> valExpr = this.config.getExpression("value", Number.class);
        if (valExpr.isPresent()) {
            Optional<? extends Number> single = valExpr.get().getSingle(ctx);
            if (single.isPresent()) {
                amount = single.get().floatValue();
            }
        }

        StaticModifier modifier = new StaticModifier(target, type, amount);

        for (Entity entity : this.entities.getArray(ctx)) {
            EntityStatMap component = EntityUtils.getComponent(entity, EntityStatMap.getComponentType());
            if (component == null) continue;

            component.putModifier(statIndex, key, modifier);
        }


        return nextStatement;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        return "apply entity stat modifier to " + this.entities.toString(ctx, debug);
    }

}
