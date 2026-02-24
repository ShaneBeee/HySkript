package com.github.skriptdev.skript.plugin;

import org.bstats.hytale.Metrics;

public class BstatsMetrics {

    public static void registerMetrics(HySk plugin) {
        Metrics metrics = new Metrics(plugin, 29735);
    }

}
