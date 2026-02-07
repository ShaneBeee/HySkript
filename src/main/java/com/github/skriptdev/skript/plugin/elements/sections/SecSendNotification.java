package com.github.skriptdev.skript.plugin.elements.sections;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.ItemWithAllMetadata;
import com.hypixel.hytale.protocol.packets.interface_.NotificationStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.io.PacketHandler;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
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

public class SecSendNotification extends CodeSection {

    public static void register(SkriptRegistration reg) {
        reg.newSection(SecSendNotification.class,
                "send notification %string/message% [to %players/playerrefs/worlds%]")
            .name("Send Notification")
            .description("Send a notification to players, worlds, or the universe.",
                "**Entries**:",
                "- `secondary-message`: Optional secondary message to display in the notification.",
                "- `itemstack`: Optional itemstack to display in the notification.",
                "- `icon`: Optional icon to display in the notification.",
                "- `style`: Optional style to use for the notification.",
                "**NOTE**: Regarding ItemStacks, if you send multiple notifications with the same ItemStack, " +
                    "the client will stack them and just keep adding quantity.")
            .examples("send notification \"Hello Player!\" to player:",
                "\titemstack: itemstack of ingredient_stick",
                "send notification \"You better be careful!!!\" to world of player:",
                "\titemstack: itemstack of deco_fire",
                "\tstyle: danger")
            .since("1.0.0")
            .register();
    }

    SectionConfiguration config = new SectionConfiguration.Builder()
        .addOptionalExpression("secondary-message", Object.class, false)
        .addOptionalExpression("itemstack", ItemStack.class, false)
        .addOptionalExpression("icon", String.class, false)
        .addOptionalExpression("style", NotificationStyle.class, false)
        .build();

    private Expression<?> message;
    private Expression<?> receivers;

    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.message = expressions[0];
        if (expressions.length > 1) {
            this.receivers = expressions[1];
        }
        return true;
    }

    @Override
    public boolean loadSection(@NotNull FileSection section, @NotNull ParserState parserState, @NotNull SkriptLogger logger) {
        return this.config.loadConfiguration(null, section, parserState, logger);
    }

    @SuppressWarnings({"removal"})
    @Override
    public Optional<? extends Statement> walk(@NotNull TriggerContext ctx) {
        Optional<? extends Statement> nextStatement = getNext();

        Object o = this.message.getSingle(ctx).orElse(null);
        Message message;
        if (o instanceof String s) {
            message = Message.raw(s);
        } else if (o instanceof Message m) {
            message = m;
        } else {
            return nextStatement;
        }

        Message secondary = null;
        Optional<Expression<Object>> sExp = this.config.getExpression("secondary-message", Object.class);
        if (sExp.isPresent()) {
            Optional<?> single = sExp.get().getSingle(ctx);
            if (single.isPresent()) {
                Object o1 = single.get();
                if (o1 instanceof String s1) {
                    secondary = Message.raw(s1);
                } else if (o1 instanceof Message m1) {
                    secondary = m1;
                }
            }
        }

        ItemStack itemStack = null;
        Optional<Expression<ItemStack>> isExp = this.config.getExpression("itemstack", ItemStack.class);
        if (isExp.isPresent()) {
            Optional<? extends ItemStack> single = isExp.get().getSingle(ctx);
            if (single.isPresent()) {
                itemStack = single.get();
            }
        }
        ItemWithAllMetadata itemWithAllMetadata = itemStack != null ? itemStack.toPacket() : null;

        String icon = null;
        Optional<Expression<String>> iconExp = this.config.getExpression("icon", String.class);
        if (iconExp.isPresent()) {
            Optional<? extends String> single = iconExp.get().getSingle(ctx);
            if (single.isPresent()) {
                icon = single.get();
            }
        }

        NotificationStyle style = NotificationStyle.Default;
        Optional<Expression<NotificationStyle>> styleExp = this.config.getExpression("style", NotificationStyle.class);
        if (styleExp.isPresent()) {
            Optional<? extends NotificationStyle> single = styleExp.get().getSingle(ctx);
            if (single.isPresent()) {
                style = single.get();
            }
        }

        if (this.receivers != null) {
            for (Object object : this.receivers.getArray(ctx)) {
                if (object instanceof World world) {
                    Store<EntityStore> store = world.getEntityStore().getStore();
                    NotificationUtil.sendNotificationToWorld(message, secondary, icon,
                        itemWithAllMetadata, style, store);
                } else if (object instanceof Player player) {
                    PacketHandler packetHandler = player.getPlayerRef().getPacketHandler();
                    NotificationUtil.sendNotification(packetHandler, message,
                        secondary, icon, itemWithAllMetadata, style);
                } else if (object instanceof PlayerRef playerRef) {
                    PacketHandler packetHandler = playerRef.getPacketHandler();
                    NotificationUtil.sendNotification(packetHandler, message,
                        secondary, icon, itemWithAllMetadata, style);
                }
            }
        } else {
            NotificationUtil.sendNotificationToUniverse(message, secondary, icon, itemWithAllMetadata, style);
        }

        return nextStatement;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String to = this.receivers != null ? this.receivers.toString(ctx, debug) : "the universe";
        return "Send notification " + this.message.toString(ctx, debug) + " to " + to;
    }

}
