package com.github.skriptdev.skript.plugin.elements.expressions.item;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemTranslationProperties;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.util.MessageUtil;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprItemStackName extends PropertyExpression<Object, String> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyExpression(ExprItemStackName.class, String.class,
                "item (:name|description)", "itemstacks/items")
            .name("Item Name/Description")
            .description("Get the name/description of an Item/ItemStack.",
                "These currently cannot be set.")
            .since("INSERT VERSION")
            .register();
    }

    private boolean name;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, ParseContext parseContext) {
        this.name = parseContext.hasMark("name");
        return super.init(expressions, matchedPattern, parseContext);
    }

    @Override
    public @Nullable String getProperty(@NotNull Object owner) {
        Item item;
        if (owner instanceof Item i) {
            item = i;
        } else if (owner instanceof ItemStack itemStack) {
            item = itemStack.getItem();
        } else {
            return null;
        }
        ItemTranslationProperties translation = item.getTranslationProperties();
        String prop = this.name ? translation.getName() : translation.getDescription();
        if (prop == null) return null;

        return MessageUtil.toAnsiString(Message.translation(prop)).toAnsi();
    }

}
