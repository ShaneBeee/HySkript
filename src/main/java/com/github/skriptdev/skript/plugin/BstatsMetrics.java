package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.skript.addon.HySkriptAddon;
import com.hypixel.hytale.common.semver.Semver;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import org.bstats.charts.DrilldownPie;
import org.bstats.hytale.Metrics;

import java.util.HashMap;
import java.util.Map;

public class BstatsMetrics {

    static void registerMetrics(HySk plugin) {
        Metrics metrics = new Metrics(plugin, 29735);

        // Plugin Version Drilldown: x.y -> x.y.z
        metrics.addCustomChart(new DrilldownPie("plugin_version_drilldown_pie", () -> {
            Semver version = plugin.getManifest().getVersion();
            DrilldownMap map = new DrilldownMap();
            map.put(version.getMajor() + "." + version.getMinor(), version.toString());
            return map.getMap();
        }));

        // Addons Drilldown: name -> version
        metrics.addCustomChart(new DrilldownPie("addons_drilldown_pie", () -> {
            DrilldownMap map = new DrilldownMap();

            for (SkriptAddon addon : SkriptAddon.getAddons()) {
                if (!(addon instanceof HySkriptAddon hySkriptAddon)) continue;

                String addonName = hySkriptAddon.getAddonName();
                String version = hySkriptAddon.getManifest().getVersion();
                map.put(addonName, version);
            }

            return map.getMap();
        }));
    }

    private static class DrilldownMap {
        private final Map<String, Map<String, Integer>> map = new HashMap<>();

        public DrilldownMap() {
        }

        public void put(String upperLabel, String lowerLabel) {
            put(upperLabel, lowerLabel, 1);
        }

        public void put(String upperLabel, String lowerLabel, int weight) {
            this.map.computeIfAbsent(upperLabel, _ -> new HashMap<>())
                .put(lowerLabel, weight);
        }

        public Map<String, Map<String, Integer>> getMap() {
            return this.map;
        }
    }

}
