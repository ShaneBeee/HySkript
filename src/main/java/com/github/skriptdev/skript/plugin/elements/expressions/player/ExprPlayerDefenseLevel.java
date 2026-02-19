package com.github.skriptdev.skript.plugin.elements.expressions.player;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems.ArmorDamageReduction;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems.ArmorDamageReduction.ArmorResistanceModifiers;
import com.hypixel.hytale.server.core.universe.world.World;
import io.github.syst3ms.skriptparser.lang.properties.PropertyExpression;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class ExprPlayerDefenseLevel extends PropertyExpression<Player, Number> {

    public static void register(SkriptRegistration reg) {
        reg.newPropertyExpression(ExprPlayerDefenseLevel.class, Number.class,
                "armor defense [level]", "players")
            .name("Player Armor Defense")
            .description("Represents the armor defense level you would see in your inventory screen.")
            .experimental("This internally uses some really janky code, and may change in the future.")
            .examples("if armor defense level of player < 10:",
                "\tkill player")
            .since("INSERT VERSION")
            .register();
    }

    @Override
    public @Nullable Number getProperty(Player player) {
        World world = player.getWorld();
        if (world == null) return null;

        // TODO find a better way to do this
        Map<DamageCause, ArmorResistanceModifiers> reduction = ArmorDamageReduction.getResistanceModifiers(world,
            player.getInventory().getArmor(), false, null);

        int defense = 0;

        DamageCause asset = DamageCause.getAssetMap().getAsset("Physical");
        ArmorResistanceModifiers mod = reduction.get(asset);
        if (mod != null) {
            defense += (int) Math.ceil(mod.multiplierModifier * 100);
        }

        return defense;
    }

}
