package com.github.skriptdev.skript.api.utils;

import com.hypixel.hytale.server.core.modules.accesscontrol.AccessControlModule;
import com.hypixel.hytale.server.core.modules.accesscontrol.provider.HytaleBanProvider;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

/**
 * Utilities for reflection operations.
 */
public class ReflectionUtils {

    private static HytaleBanProvider BAN_PROVIDER;

    /**
     * @hidden
     */
    public static void init() {
        AccessControlModule accessControlModule = AccessControlModule.get();
        try {
            Field banProvider = AccessControlModule.class.getDeclaredField("banProvider");
            banProvider.setAccessible(true);
            BAN_PROVIDER = (HytaleBanProvider) banProvider.get(accessControlModule);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            Utils.error("Failed to get HytaleBanProvider: %s", e.getMessage());
        }
    }

    /**
     * Get access to the HytaleBanProvider.
     * <br>This is currently private with no getter.
     *
     * @return HytaleBanProvider
     */
    public static @Nullable HytaleBanProvider getBanProvider() {
        return BAN_PROVIDER;
    }

}
