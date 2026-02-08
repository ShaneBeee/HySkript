package com.github.skriptdev.skript.plugin.command;

import com.github.skriptdev.skript.api.skript.event.PlayerContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.permissions.provider.PermissionProvider;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.log.LogEntry;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.parsing.ParserState;
import io.github.syst3ms.skriptparser.parsing.SyntaxParser;
import io.github.syst3ms.skriptparser.registration.context.ContextValue.Usage;
import io.github.syst3ms.skriptparser.variables.Variables;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class EffectCommands {

    public static void register(Skript skript, String token, boolean allowOps, String permission) {
        skript.getSkriptRegistration().newSingleContextValue(PlayerEffectContext.class, Player.class, "me", PlayerEffectContext::getPlayer)
            .setUsage(Usage.EXPRESSION_OR_ALONE)
            .register();

        skript.getPlugin().getEventRegistry().registerGlobal(PlayerChatEvent.class, event -> {
            if (event.getContent().startsWith(token)) {
                PlayerRef sender = event.getSender();

                // PERM CHECK
                PermissionsModule perm = PermissionsModule.get();
                PermissionProvider provider = perm.getFirstPermissionProvider();
                Set<String> groupsForUser = perm.getGroupsForUser(sender.getUuid());

                // If the player is an operator and allowOps is false, check their group/user permissions
                if (!allowOps && groupsForUser.contains("OP")) {
                    boolean can = false;
                    // If the user explicitely has this permission, they can use effect commands
                    if (provider.getUserPermissions(sender.getUuid()).contains(permission)) {
                        can = true;
                    } else {
                        // Check all groups
                        for (String group : groupsForUser) {
                            // If the group explicitely has this permission, they can use effect commands
                            if (provider.getGroupPermissions(group).contains(permission)) {
                                can = true;
                                break;
                            }
                        }
                    }
                    if (!can) return;
                } else if (!perm.hasPermission(sender.getUuid(), permission)) {
                    return;
                }

                event.setCancelled(true);

                // Create dummy ParserState/Logger for effect commands
                ParserState parserState = new ParserState();
                parserState.setCurrentContexts(Set.of(PlayerEffectContext.class));
                SkriptLogger skriptLogger = new SkriptLogger(true);
                skriptLogger.setFileInfo("dummy_cause_this_doesnt_matter.sk", List.of());

                // Parse effect
                String effectString = event.getContent().substring(1);
                Optional<? extends Effect> optionalEffect = SyntaxParser.parseEffect(effectString, parserState, skriptLogger);

                // If no effect available, send logs
                if (optionalEffect.isEmpty()) {
                    skriptLogger.finalizeLogs();
                    for (LogEntry logEntry : skriptLogger.close()) {
                        Utils.log(sender, logEntry);
                    }
                    return;
                }

                Effect effect = optionalEffect.get();
                Ref<EntityStore> reference = sender.getReference();
                if (reference == null) return;

                UUID worldUuid = sender.getWorldUuid();
                if (worldUuid == null) return;

                World world = Universe.get().getWorld(worldUuid);
                if (world == null) return;

                skriptLogger.info("Executing: '" + effectString + "'");
                skriptLogger.finalizeLogs();
                for (LogEntry logEntry : skriptLogger.close()) {
                    Utils.log(sender, logEntry);
                }

                Runnable code = () -> {
                    Player player = world.getEntityStore().getStore().getComponent(reference, Player.getComponentType());
                    PlayerEffectContext effectContext = new PlayerEffectContext(player);
                    effect.walk(effectContext);
                    // Clear local variables to prevent memory leaks
                    Variables.clearLocalVariables(effectContext);
                };

                if (world.isInThread()) {
                    code.run();
                } else {
                    world.execute(code);
                }

            }
        });
    }

    private record PlayerEffectContext(Player player) implements PlayerContext {

        public Player getPlayer() {
            return this.player;
        }

        @Override
        public String getName() {
            return "player effect context";
        }
    }

}
