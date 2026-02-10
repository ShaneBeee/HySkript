package com.github.skriptdev.skript.api.skript;

import com.github.skriptdev.skript.api.utils.Utils;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.registration.SkriptEventInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.SyntaxManager;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ErrorHandler {

    private static final Map<String, SkriptEventInfo<?>> EVENT_MAP = new HashMap<>();
    private static final Map<String, SyntaxInfo<? extends Effect>> EFFECT_MAP = new HashMap<>();

    public static void init() {
        for (SyntaxInfo<? extends Effect> effect : SyntaxManager.getEffects()) {
            EFFECT_MAP.put(effect.getSyntaxClass().getSimpleName(), effect);
        }
        for (SkriptEventInfo<?> event : SyntaxManager.getTriggers()) {
            EVENT_MAP.put(event.getSyntaxClass().getSimpleName(), event);
        }
        setup();
    }

    private static @Nullable SkriptEventInfo<?> getEvent(String name) {
        return EVENT_MAP.get(name);
    }

    private static @Nullable SyntaxInfo<? extends Effect> getEffect(String name) {
        return EFFECT_MAP.get(name);
    }

    private static void setup() {
        // Set up how HySkript handles IllegalStateExceptions
        Statement.setIllegalStateHandler(e -> {
            String message = e.getMessage();
            if (message.contains("Assert not in thread!")) {
                // Hytale has threads for each world, effects involving a world must happen on a world thread
                boolean failedInCommand = false;
                for (StackTraceElement ste : e.getStackTrace()) {
                    if (ste.getClassName().contains("ScriptCommandBuilder")) {
                        failedInCommand = true;
                        break;
                    }
                }
                if (failedInCommand) {
                    Utils.logToAdmins(Level.WARNING, "A command was executed on the wrong thread, see console for more info.");
                    Utils.error("A command was executed on the wrong thread!");
                    Utils.warn("If you have a regular/global command that a player is running, which executes code in a world, consider:");
                    Utils.warn("  - Using the 'player command' or 'world command' command types.");
                    Utils.warn("  - Using 'execute in %world%' section.");
                    Utils.error("Original error message: %s", message);
                } else {
                    Utils.logToAdmins(Level.WARNING, "Something was executed on the wrong thread.");
                    if (message.contains("World")) {
                        Utils.error("A world was accessed on the wrong thread!");
                        Utils.warn("Consider using 'execute in %world%' section.");
                        Utils.error("Original error message: %s", message);
                    }
                }
            } else if (message.contains("calling a store method from a system")) {
                // Hytale doesn't allow store methods to be called within System-based events
                String eventName = "unknown event";
                String effectName = "unknown effect";

                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    String fileName = stackTraceElement.getFileName();
                    if (fileName == null) continue;
                    fileName = fileName.replace(".java", "");

                    // Try to find the event
                    if (eventName.contains("unknown")) {
                        SkriptEventInfo<?> event = getEvent(fileName);
                        if (event != null) {
                            eventName = event.getDocumentation().getName();
                            if (eventName == null) eventName = event.getClass().getSimpleName();
                        }
                    }

                    // Try to find the effect
                    if (effectName.contains("unknown")) {
                        SyntaxInfo<? extends Effect> effect = getEffect(fileName);
                        if (effect != null) {
                            effectName = effect.getDocumentation().getName();
                            if (effectName == null) effectName = effect.getClass().getSimpleName();
                        }

                    }

                }
                Utils.logToAdmins(Level.WARNING, "An effect was executed incorrectly, see console for more info.");
                Utils.error("The effect '%s' was executed incorrectly in the event '%s'.", effectName, eventName);
                Utils.error("Hytale doesn't allow Store-based effects to be executed within System-based events.");
                Utils.warn("Consider waiting a tick before using this effect to prevent this.");

            } else {
                Utils.error("An error ocrrured while executing a script: %s", message);
                for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                    Utils.error("  - %s", stackTraceElement);
                }
            }
        });
    }

}
