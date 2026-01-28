package com.github.skriptdev.skript.api.utils;

import com.github.skriptdev.skript.plugin.HySk;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.receiver.IMessageReceiver;
import fi.sulku.hytale.TinyMsg;
import io.github.syst3ms.skriptparser.log.LogEntry;

import java.awt.*;
import java.util.logging.Level;

public class Utils {

    static final Message CORE_PREFIX = TinyMsg.parse("<color:736E6E>[<gradient:07CAE5:0DD22B>HySkript<color:736E6E>] ");

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
        log(null, level, message, args);
    }

    public static void log(IMessageReceiver receiver, Level level, String message, Object... args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        if (receiver == null) {
            HySk.getInstance().getLogger().at(level).log(message);
        } else {
            Color color = level == Level.SEVERE ? Color.RED : level == Level.WARNING ? Color.YELLOW : level == Level.FINE ? Color.PINK : Color.WHITE;
            Message coloredMessage = Message.raw(message).color(color);

            Message m = Message.empty().insert(CORE_PREFIX).insert(coloredMessage);
            receiver.sendMessage(m);
        }
    }

    public static void log(String message, Object... args) {
        log(null, Level.INFO, message, args);
    }

    public static void log(IMessageReceiver receiver, String message, Object... args) {
        log(receiver, Level.INFO, message, args);
    }

    public static void log(IMessageReceiver receiver, LogEntry logEntry) {
        String message = logEntry.getMessage();
        switch (logEntry.getType()) {
            case DEBUG -> log(receiver, Level.FINE, message);
            case INFO -> log(receiver, Level.INFO, message);
            case ERROR -> log(receiver, Level.SEVERE, message);
            case WARNING -> log(receiver, Level.WARNING, message);
        }
    }

    public static void error(String message, Object... args) {
        error(null, message, args);
    }

    public static void error(IMessageReceiver receiver, String message, Object... args) {
        log(receiver, Level.SEVERE, message, args);
    }

    public static void warn(String message, Object... args) {
        warn(null, message, args);
    }

    public static void warn(IMessageReceiver receiver, String message, Object... args) {
        log(receiver, Level.WARNING, message, args);
    }

}
