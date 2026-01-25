package com.github.skriptdev.skript.plugin.elements.events;

import com.github.skriptdev.skript.plugin.elements.command.ScriptCommand;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;

public class EventHandler {

    public static void register(SkriptRegistration registration) {
        ScriptCommand.register(registration);
        EvtLoad.register(registration);
        EvtPlayerChat.register(registration);
        EvtPlayerJoin.register(registration);
    }

}
