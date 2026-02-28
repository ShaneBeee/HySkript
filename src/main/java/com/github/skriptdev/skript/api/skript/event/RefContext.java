package com.github.skriptdev.skript.api.skript.event;

import com.hypixel.hytale.component.Ref;
import io.github.syst3ms.skriptparser.lang.TriggerContext;

public interface RefContext<E> extends TriggerContext {

    Ref<E> getRef();

}
