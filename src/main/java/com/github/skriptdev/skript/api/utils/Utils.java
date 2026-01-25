package com.github.skriptdev.skript.api.utils;

import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import io.github.syst3ms.skriptparser.log.LogEntry;

import java.awt.*;
import java.util.logging.Level;

public class Utils {

    private static final Color BRACKET_COLOR = new Color(115, 110, 110);
    private static final Color CORE_NAME_COLOR = new Color(27, 169, 140);
    private static final Message CORE_PREFIX = Message.raw("[").color(BRACKET_COLOR)
        .insert(Message.raw("HySk").color(CORE_NAME_COLOR))
        .insert(Message.raw("] ").color(BRACKET_COLOR));

    /**
     * Send a message to a receiver.
     * Message will be prefixed
     *
     * @param receiver Receiver to send a message to
     * @param message  Message to send
     */
    public static void sendMessage(IMessageReceiver receiver, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        receiver.sendMessage(Message.raw(message));
    }

    public static void log(Level level, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        HySk.getInstance().getLogger().at(level).log(message);
    }

    public static void log(String message, Object... args) {
        log(Level.INFO, message, args);
    }

    public static void log(LogEntry logEntry) {
        String message = logEntry.getMessage();
        switch (logEntry.getType()) {
            case DEBUG -> log(Level.FINE, message);
            case INFO -> log(message);
            case ERROR -> error(message);
            case WARNING -> warn(message);
        }
    }

    public static void error(String message, Object... args) {
        log(Level.SEVERE, message, args);
    }

    public static void warn(String message, Object... args) {
        log(Level.WARNING, message, args);
    }

}
