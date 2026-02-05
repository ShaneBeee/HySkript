package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.TypeManager;

public class Types {

    public static void register(SkriptRegistration registration) {
        TypesJava.register(registration);
        TypesCustom.register(registration);
        TypesServer.register(registration);
        TypesEntity.register(registration);
        TypesItem.register(registration);
        TypesBlock.register(registration);
        TypesWorld.register(registration);
        TypesAssetStore.register(registration);

        TypeManager.register(registration);
    }

}
