package com.github.skriptdev.skript.plugin;

import com.github.skriptdev.skript.api.skript.ScriptsLoader;
import com.github.skriptdev.skript.api.utils.ReflectionUtils;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.elements.ElementRegistration;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.lang.Trigger;
import io.github.syst3ms.skriptparser.log.SkriptLogger;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class Skript extends SkriptAddon {

    public static Skript INSTANCE;
    private final HySk hySk;
    private final Path scriptsPath;
    private final SkriptLogger logger;
    private SkriptRegistration registration;
    private ElementRegistration elementRegistration;
    private ScriptsLoader scriptsLoader;

    public Skript(HySk hySk) {
        INSTANCE = this;
        this.hySk = hySk;
        this.scriptsPath = hySk.getDataDirectory().resolve("scripts");
        this.logger = new SkriptLogger(true);

        Utils.log("Setting up HySkript!");
        setup();
    }

    private void setup() {
        ReflectionUtils.init();
        this.registration = new SkriptRegistration(this);
        this.elementRegistration = new ElementRegistration(this);
        this.elementRegistration.registerElements();

        // FINALIZE SETUP
        this.registration.register();

        printSyntaxCount();
        Utils.log("HySkript setup complete!");

        // LOAD SCRIPTS
        this.scriptsLoader = new ScriptsLoader(this);
        this.scriptsLoader.loadScripts(this.scriptsPath, false);
    }

    private void printSyntaxCount() {
        SkriptRegistration mainRegistration = Parser.getMainRegistration();

        int eventSize = this.registration.getEvents().size() + mainRegistration.getEvents().size();
        int effectSize = this.registration.getEffects().size() + mainRegistration.getEffects().size();
        int expsSize = this.registration.getExpressions().size() + mainRegistration.getExpressions().size();
        int secSize = this.registration.getSections().size() + mainRegistration.getSections().size();
        int typeSize = this.registration.getTypes().size() + mainRegistration.getTypes().size();

        int total = eventSize + effectSize + expsSize + secSize + typeSize;

        Utils.log("Loaded HySkript %s elements:", total);
        Utils.log("- Types: %s", typeSize);
        Utils.log("- Events: %s ", eventSize);
        Utils.log("- Effects: %s", effectSize);
        Utils.log("- Expressions: %s", expsSize);
        Utils.log("- Sections: %s", secSize);
    }

    public HySk getPlugin() {
        return this.hySk;
    }

    public Path getScriptsPath() {
        return this.scriptsPath;
    }

    public SkriptLogger getLogger() {
        return this.logger;
    }

    public SkriptRegistration getRegistration() {
        return this.registration;
    }

    public ElementRegistration getElementRegistration() {
        return this.elementRegistration;
    }

    public ScriptsLoader getScriptsLoader() {
        return this.scriptsLoader;
    }

    @Override
    public void handleTrigger(@NotNull Trigger trigger) {
        this.elementRegistration.handleTrigger(trigger);
    }

}
