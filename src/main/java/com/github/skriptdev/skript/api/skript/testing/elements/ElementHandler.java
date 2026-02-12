package com.github.skriptdev.skript.api.skript.testing.elements;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;

public class ElementHandler {

    public static void register(SkriptRegistration reg) {
        EffAssert.register(reg);
        EvtTest.register(reg);
    }

}
