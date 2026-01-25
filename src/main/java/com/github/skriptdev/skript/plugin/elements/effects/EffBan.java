package com.github.skriptdev.skript.plugin.elements.effects;

import com.github.skriptdev.skript.api.utils.ReflectionUtils;
import com.github.skriptdev.skript.api.utils.Utils;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.Ban;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.InfiniteBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.ban.TimedBan;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Expression;
import io.github.syst3ms.skriptparser.lang.TriggerContext;
import io.github.syst3ms.skriptparser.parsing.ParseContext;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class EffBan extends Effect {

    private static HytaleBanProvider BAN_PROVIDER;

    public static void register(SkriptRegistration registration) {
        BAN_PROVIDER = ReflectionUtils.getBanProvider();
        if (BAN_PROVIDER == null) {
            Utils.error("Could not find HytaleBanProvider. Skipping Ban effect registration.");
            return;
        }
        registration.newEffect(EffBan.class,
                "ban %players/playerrefs/uuids%",
                "ban %players/playerrefs/uuids% by %commandsender%",
                "ban %players/playerrefs/uuids% for %duration%",
                "ban %players/playerrefs/uuids% for %duration% by %commandsender%",
                "ban %players/playerrefs/uuids% (for reason|due to|because) %string%",
                "ban %players/playerrefs/uuids% (for reason|due to|because) %string% by %commandsender%",
                "ban %players/playerrefs/uuids% for %duration% (for reason|due to|because) %string%",
                "ban %players/playerrefs/uuids% for %duration% (for reason|due to|because) %string% by %commandsender%",
                "unban %players/playerrefs/uuids%")
            .name("Ban Player")
            .description("Ban/unban the specified players with an optional reason, duration and by whom.",
                "If the sender is not specified, the ban will be issued by the console.",
                "If the duration is not specified, the ban will be permanent.")
            .examples("ban player",
                "ban {_player} by player",
                "ban player for 10 minutes",
                "ban {_player} for 1 week by {_player}",
                "ban player due to \"You cheated!\"",
                "ban {_someUUID} due to \"You cheated!\" by player",
                "ban {_p} for 1 hour due to \"You cheated!\"",
                "ban {_p} for 10 minutes due to \"Be nicer in chat\" by player",
                "unban player",
                "unban {_uuid}")
            .since("INSERT VERSION")
            .register();
    }

    private boolean unban;
    private Expression<?> players;
    private Expression<String> reason;
    private Expression<Duration> duration;
    private Expression<CommandSender> sender;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, @NotNull ParseContext parseContext) {
        this.players = expressions[0];
        this.unban = matchedPattern == 8;
        switch (matchedPattern) {
            case 1:
                this.sender = (Expression<CommandSender>) expressions[1];
                break;
            case 2:
                this.duration = (Expression<Duration>) expressions[1];
                break;
            case 3:
                this.duration = (Expression<Duration>) expressions[1];
                this.sender = (Expression<CommandSender>) expressions[2];
                break;
            case 4:
                this.reason = (Expression<String>) expressions[1];
                break;
            case 5:
                this.reason = (Expression<String>) expressions[1];
                this.sender = (Expression<CommandSender>) expressions[2];
                break;
            case 6:
                this.duration = (Expression<Duration>) expressions[1];
                this.reason = (Expression<String>) expressions[2];
                break;
            case 7:
                this.duration = (Expression<Duration>) expressions[1];
                this.reason = (Expression<String>) expressions[2];
                this.sender = (Expression<CommandSender>) expressions[3];
        }
        return true;
    }

    @Override
    protected void execute(@NotNull TriggerContext ctx) {
        if (this.unban) {
            for (Object o : this.players.getArray(ctx)) {
                UUID uuid = getTarget(o);
                BAN_PROVIDER.modify(uuidBanMap -> {
                    uuidBanMap.remove(uuid);
                    return true;
                });
            }
            return;
        }
        CommandSender sender = ConsoleSender.INSTANCE;
        if (this.sender != null) {
            Optional<? extends CommandSender> single = this.sender.getSingle(ctx);
            if (single.isPresent()) sender = single.get();
        }

        String reason = "You were banned.";
        if (this.reason != null) {
            Optional<? extends String> single = this.reason.getSingle(ctx);
            if (single.isPresent()) reason = single.get();
        }

        Duration duration = null;
        if (this.duration != null) {
            Optional<? extends Duration> single = this.duration.getSingle(ctx);
            if (single.isPresent()) duration = single.get();
        }

        Map<UUID, Ban> bans = new HashMap<>();
        for (Object o : this.players.getArray(ctx)) {
            UUID target = getTarget(o);
            if (target == null) continue;

            Ban ban;
            if (duration != null) {
                Instant end = Instant.now().plus(duration);
                ban = new TimedBan(target, sender.getUuid(), Instant.now(), end, reason);
            } else {
                ban = new InfiniteBan(target, sender.getUuid(), Instant.now(), reason);
            }

            bans.put(target, ban);
            EffKick.kick(o, reason);
        }

        // Put them all in at once
        // It saves the file each time one is added
        BAN_PROVIDER.modify(uuidBanMap -> {
            uuidBanMap.putAll(bans);
            return true;
        });
    }

    @SuppressWarnings("removal")
    private UUID getTarget(Object o) {
        if (o instanceof UUID uuid) return uuid;
        else if (o instanceof PlayerRef ref) return ref.getUuid();
        else if (o instanceof Player player) return player.getPlayerRef().getUuid();
        return null;
    }

    @Override
    public String toString(@NotNull TriggerContext ctx, boolean debug) {
        String duration = this.duration == null ? "" : " for " + this.duration.toString(ctx, debug);
        String reason = this.reason == null ? "" : " for reason " + this.reason.toString(ctx, debug);
        String sender = this.sender == null ? "" : " by " + this.sender.toString(ctx, debug);
        return "ban " + this.players.toString(ctx, debug) + duration + reason + sender;
    }

}
