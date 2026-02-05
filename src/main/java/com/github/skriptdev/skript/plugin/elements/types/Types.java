package com.github.skriptdev.skript.plugin.elements.types;

import com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.types.TypeManager;

public class Types {

    public static void register(SkriptRegistration registration) {
        // Keep these in alphabetical order
        TypesAssetStore.register(registration);
        TypesBlock.register(registration);
        TypesCustom.register(registration);
        TypesEntity.register(registration);
        TypesItem.register(registration);
        TypesJava.register(registration);
        TypesPlayer.register(registration);
        TypesServer.register(registration);
        TypesWorld.register(registration);

        TypeManager.register(registration);
    }

}
