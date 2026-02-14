package com.github.skriptdev.skript.api.hytale.utils;

import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class StoreUtils {

    @SuppressWarnings("unchecked")
    public static <T> CommandBuffer<T> getCommandBuffer(Store<T> store) {
        try {
            Method method = store.getClass().getDeclaredMethod("takeCommandBuffer");
            method.setAccessible(true);
            return (CommandBuffer<T>) method.invoke(store);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
