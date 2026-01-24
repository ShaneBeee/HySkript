package com.github.skriptdev.skript.plugin.elements.listeners;

import com.github.skriptdev.skript.api.skript.eventcontext.ScriptLoadContext;
import com.github.skriptdev.skript.plugin.Skript;
import com.github.skriptdev.skript.plugin.elements.events.EvtLoad;
import com.github.skriptdev.skript.plugin.elements.events.EvtPlayerJoin;
import com.hypixel.hytale.event.EventRegistry;
import io.github.syst3ms.skriptparser.event.EvtPeriodical;
import io.github.syst3ms.skriptparser.event.PeriodicalContext;
import io.github.syst3ms.skriptparser.lang.SkriptEvent;
import io.github.syst3ms.skriptparser.lang.Statement;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.util.ThreadUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ListenerHandler {

    private final Skript skript;
    private final PlayerJoinListener playerJoinListener;
    private final PlayerListener playerListener;
    private final List<Trigger> onLoadTriggers = new ArrayList<>();
    private final List<Trigger> periodicalTriggers = new ArrayList<>();

    public ListenerHandler(Skript skript) {
        this.skript = skript;
        EventRegistry eventRegistry = skript.getPlugin().getEventRegistry();
        this.playerListener = new PlayerListener(eventRegistry);
        this.playerJoinListener = new PlayerJoinListener(eventRegistry);
    }

    public void handleTrigger(Trigger trigger) {
        SkriptEvent event = trigger.getEvent();

        if (!this.skript.canHandleEvent(event))
            return;

        switch (event) {
            case EvtLoad ignored -> this.onLoadTriggers.add(trigger);
            case EvtPeriodical ignored -> this.periodicalTriggers.add(trigger);
            case EvtPlayerJoin evtPlayerJoin -> this.playerJoinListener.addTrigger(trigger, evtPlayerJoin.getPattern());
            default -> this.playerListener.handleTrigger(trigger);
        }
    }

    public void finishedLoading() {
        for (Trigger trigger : this.onLoadTriggers) {
            Statement.runAll(trigger, new ScriptLoadContext());
        }
        for (Trigger trigger : this.periodicalTriggers) {
            PeriodicalContext ctx = new PeriodicalContext();
            Duration dur = ((EvtPeriodical) trigger.getEvent()).getDuration().getSingle(ctx).orElseThrow(AssertionError::new);
            ThreadUtils.runPeriodically(() -> Statement.runAll(trigger, ctx), dur);
        }
    }

    public void clearTriggers() {
        this.onLoadTriggers.clear();
        this.periodicalTriggers.clear();
        this.playerJoinListener.clearTriggers();
        this.playerListener.clearTriggers();
    }

}
