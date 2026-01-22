package com.shanebeestudios.skript.api.skript.eventcontext;

import io.github.syst3ms.skriptparser.lang.TriggerContext;

public class EventContext implements TriggerContext {

    @Override
    public String getName() {
        return "regular event";
    }

}
