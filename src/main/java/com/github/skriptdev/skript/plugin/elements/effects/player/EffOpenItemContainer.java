package com.github.skriptdev.skript.plugin.elements.effects.player;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerWindow;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import org.jetbrains.annotations.NotNull;

public class EffOpenItemContainer extends Effect {

    public static void register(SkriptRegistration reg) {
        reg.newEffect(EffOpenItemContainer.class,
                "open %itemContainer% to %players%",
                "open %itemContainer% to %players% (with|using) page %page%")
            .name("Open ItemContainer")
            .description("Opens an ItemContainer to the specified players.",
                "You can optionally choose a page type to use (Default is Bench).",
                "Don't use the `custom` page, it'll kick the player.")
            .examples("open storage item container of inventory of player to player")
            .since("INSERT VERSION")
            .register();
    }

    private Expression<ItemContainer> itemContainer;
    private Expression<Player> players;
    private Expression<Page> page;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?> @NotNull [] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.itemContainer = (Expression<ItemContainer>) expressions[0];
        this.players = (Expression<Player>) expressions[1];
        if (matchedPattern == 1) {
            this.page = (Expression<Page>) expressions[2];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        ItemContainer container = this.itemContainer.getSingle(ctx).orElse(null);
        if (container == null) return;

        Page page = Page.Bench;
        if (this.page != null) {
            Page page1 = this.page.getSingle(ctx).orElse(null);
            if (page1 != null) page = page1;
        }

        for (Player player : this.players.getArray(ctx)) {
            openContainer(container, player, page);
        }
    }

    private void openContainer(ItemContainer container, Player player, Page page) {
        Ref<EntityStore> ref = player.getReference();
        if (ref == null) return;

        Store<EntityStore> store = ref.getStore();

        player.getPageManager().setPageWithWindows(ref, store, page,
            true, new ContainerWindow(container));
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String page = this.page != null ? " with page " + this.page.toString(ctx, debug) : "";
        return "open item container " + this.itemContainer.toString(ctx, debug) +
            " to " + players.toString(ctx, debug) + page;
    }

}
