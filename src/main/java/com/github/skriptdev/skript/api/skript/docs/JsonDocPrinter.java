package com.github.skriptdev.skript.api.skript.docs;

import com.github.skriptdev.skript.api.skript.addon.HySkriptAddon;
import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.github.skriptdev.skript.plugin.Skript;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.util.BsonUtil;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.docs.Documentation;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Structure;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
import io.github.syst3ms.skriptparser.registration.SkriptAddon;
import io.github.syst3ms.skriptparser.registration.SkriptRegistration;
import io.github.syst3ms.skriptparser.registration.SyntaxInfo;
import io.github.syst3ms.skriptparser.registration.context.ContextValue;
import io.github.syst3ms.skriptparser.structures.functions.Function;
import io.github.syst3ms.skriptparser.structures.functions.FunctionParameter;
import io.github.syst3ms.skriptparser.structures.functions.Functions;
import io.github.syst3ms.skriptparser.structures.functions.JavaFunction;
import io.github.syst3ms.skriptparser.types.PatternType;
import io.github.syst3ms.skriptparser.types.Type;
import io.github.syst3ms.skriptparser.types.TypeManager;
import io.github.syst3ms.skriptparser.util.StringUtils;
import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class JsonDocPrinter {

    private final CommandSender sender;
    private final SkriptRegistration registration;
    private final SkriptAddon addon;
    private final String addonKey;
    private final boolean includeSkriptParser;

    public JsonDocPrinter(CommandSender sender, SkriptAddon addon) {
        this.sender = sender;
        this.registration = addon.getSkriptRegistration();
        this.addon = registration.getRegisterer();
        this.addonKey = this.addon.getAddonName().toLowerCase(Locale.ROOT).replace(" ", "_");
        this.includeSkriptParser = registration instanceof com.github.skriptdev.skript.api.skript.registration.SkriptRegistration;
    }

    public void printDocs() {
        long start = System.currentTimeMillis();
        String addonName = this.addon.getAddonName();
        Utils.log(this.sender, "Printing documentation for '%s'", addonName);
        SkriptRegistration parserRegistration = Parser.getMainRegistration();

        BsonDocument mainDocs = new BsonDocument();

        // EVENTS
        Utils.log(this.sender, "Printing events");
        printEvents(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printEvents(mainDocs, parserRegistration);
        }

        // STRUCTURES
        Utils.log(this.sender, "Printing structures");
        printStructures(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printStructures(mainDocs, parserRegistration);
        }

        // EXPRESSIONS
        Utils.log(this.sender, "Printing expressions and conditions");
        printExpressions(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printExpressions(mainDocs, parserRegistration);
        }

        // FUNCTIONS
        Utils.log(this.sender, "Printing functions");
        printFunctions(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printFunctions(mainDocs, parserRegistration);
        }

        // EFFECTS
        Utils.log(this.sender, "Printing effects");
        printEffects(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printEffects(mainDocs, parserRegistration);
        }

        // TYPES
        Utils.log(this.sender, "Printing types");
        printTypes(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printTypes(mainDocs, parserRegistration);
        }

        // SECTIONS
        Utils.log(this.sender, "Printing sections");
        printSections(mainDocs, this.registration);
        if (this.includeSkriptParser) {
            printSections(mainDocs, parserRegistration);
        }

        // METADATA
        BsonDocument meta = mainDocs.getDocument("metadata", new BsonDocument());
        String version = "unknown";
        if (this.addon instanceof HySkriptAddon hySkriptAddon) {
            version = hySkriptAddon.getManifest().getVersion();
        } else if (this.addon instanceof Skript skript) {
            version = skript.getPlugin().getManifest().getVersion().toString();
        }
        meta.put("version", new BsonString(version));
        mainDocs.put("metadata", meta);

        // FINISHED
        Utils.log(this.sender, "Writing documentation to 'docs/%s/docs.json' file", addonName);
        File file = getFile();
        BsonUtil.writeDocument(file.toPath(), mainDocs, false);
        Utils.log(this.sender, "Done creating docs in %sms!", System.currentTimeMillis() - start);
    }

    private void printEvents(BsonDocument mainDocs, SkriptRegistration registration) {
        BsonArray eventsArray = mainDocs.getArray("events", new BsonArray());

        List<ContextValue<?, ?>> allContextValues = registration.getContextValues();

        registration.getEvents().forEach(event -> {
            Documentation documentation = event.getDocumentation();
            if (documentation.isNoDoc()) return;

            BsonDocument eventDoc = new BsonDocument();

            if (Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                return;
            }
            printDocumentation("event", eventDoc, event);

            AtomicBoolean cancellable = new AtomicBoolean(false);
            event.getContexts().forEach(context -> {
                if (CancellableContext.class.isAssignableFrom(context)) {
                    cancellable.set(true);
                }
            });
            eventDoc.put("cancellable", new BsonBoolean(cancellable.get()));

            // CONTEXT VALUES
            List<ContextValue<?, ?>> valuesForThisEvent = new ArrayList<>();
            allContextValues.forEach(contextValue -> {
                if (event.getContexts().contains(contextValue.getContext())) {
                    valuesForThisEvent.add(contextValue);
                }
            });
            BsonArray contextValues = eventDoc.getArray("context values", new BsonArray());

            if (!valuesForThisEvent.isEmpty()) {
                // Append context values/description/type to the main description
                BsonArray description = eventDoc.getArray("description", new BsonArray());
                description.add(new BsonString(""));
                description.add(new BsonString("**Context Values:**"));

                valuesForThisEvent.forEach(contextValue -> {
                    ContextValue.State state = contextValue.getState();
                    String contextState = state == ContextValue.State.PRESENT ? "" : state.name().toLowerCase(Locale.ROOT) + " ";
                    contextValues.add(new BsonString(contextState + "context-" + contextValue.getPattern().toString()));
                    description.add(new BsonString(getContextValueDescription(contextValue)));
                });
                eventDoc.put("description", description);

            }
            eventDoc.put("context values", contextValues);

            eventsArray.add(eventDoc);
        });
        mainDocs.put("events", eventsArray);
    }

    private void printStructures(BsonDocument mainDocs, SkriptRegistration registration) {
        BsonArray structuresArray = mainDocs.getArray("structures", new BsonArray());
        List<ContextValue<?, ?>> allContextValues = registration.getContextValues();

        registration.getStructures().forEach(event -> {
            Documentation documentation = event.getDocumentation();
            if (documentation.isNoDoc()) return;

            if (!Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                return;
            }

            BsonDocument structureDoc = new BsonDocument();
            printDocumentation("structure", structureDoc, event);
            structuresArray.add(structureDoc);

            // CONTEXT VALUES
            List<ContextValue<?, ?>> valuesForThisStructure = new ArrayList<>();
            allContextValues.forEach(contextValue -> {
                if (event.getContexts().contains(contextValue.getContext())) {
                    valuesForThisStructure.add(contextValue);
                }
            });
            BsonArray contextValues = structureDoc.getArray("context values", new BsonArray());

            if (!valuesForThisStructure.isEmpty()) {
                // Append context values/description/type to the main description
                BsonArray description = structureDoc.getArray("description", new BsonArray());
                description.add(new BsonString(""));
                description.add(new BsonString("**Context Values:**"));

                valuesForThisStructure.forEach(contextValue -> {
                    ContextValue.State state = contextValue.getState();
                    String contextState = state == ContextValue.State.PRESENT ? "" : state.name().toLowerCase(Locale.ROOT) + " ";
                    contextValues.add(new BsonString(contextState + "context-" + contextValue.getPattern().toString()));
                    description.add(new BsonString(getContextValueDescription(contextValue)));
                });
                structureDoc.put("description", description);

            }
            structureDoc.put("context values", contextValues);
        });

        mainDocs.put("structures", structuresArray);
    }

    private void printExpressions(BsonDocument mainDocs, SkriptRegistration registration) {
        List<List<ExpressionInfo<?, ?>>> values = new ArrayList<>(registration.getExpressions().values());

        BsonArray exprsArray = mainDocs.getArray("expressions", new BsonArray());
        BsonArray condArray = mainDocs.getArray("conditions", new BsonArray());

        for (List<ExpressionInfo<?, ?>> expressionInfos : values) {
            expressionInfos.forEach(expressionInfo -> {
                Documentation documentation = expressionInfo.getDocumentation();
                if (documentation.isNoDoc()) return;

                BsonDocument expressionDoc = new BsonDocument();

                // Return Type
                Type<?> returnType = expressionInfo.getReturnType().getType();
                String returnTypeName = returnType.getDocumentation().getName();
                if (returnTypeName == null) returnTypeName = returnType.getBaseName();
                expressionDoc.put("return type", new BsonString(returnTypeName));

                Class<?> syntaxClass = expressionInfo.getSyntaxClass();
                if (ExecutableExpression.class.isAssignableFrom(syntaxClass)) {
                    // TODO new section on the docs?!?!?!
                    exprsArray.add(expressionDoc);
                    printDocumentation("executable-expression", expressionDoc, expressionInfo);
                } else if (ConditionalExpression.class.isAssignableFrom(syntaxClass)) {
                    condArray.add(expressionDoc);
                    printDocumentation("condition", expressionDoc, expressionInfo);
                } else {
                    exprsArray.add(expressionDoc);
                    printDocumentation("expression", expressionDoc, expressionInfo);

                }
            });
        }
        mainDocs.put("expressions", exprsArray);
        mainDocs.put("conditions", condArray);
    }

    @SuppressWarnings("unchecked")
    private void printFunctions(BsonDocument mainDocs, SkriptRegistration registration) {
        BsonArray functionsArray = mainDocs.getArray("functions", new BsonArray());
        Functions.getJavaFunctions(registration).stream().sorted(Comparator.comparing(Function::getName)).forEach(function -> {
            if (function instanceof JavaFunction<?> jf) {
                Documentation documentation = jf.getDocumentation();
                if (documentation.isNoDoc()) return;

                BsonDocument functionDoc = new BsonDocument();
                String name = documentation.getName();
                if (name == null) name = function.getName();

                functionDoc.put("name", new BsonString(name));
                functionDoc.put("id", getId("function", name));

                // Create params
                FunctionParameter<?>[] parameters = jf.getParameters();

                List<String> parameterNames = new ArrayList<>();
                for (FunctionParameter<?> parameter : parameters) {
                    Optional<? extends Type<?>> byClass = TypeManager.getByClass(parameter.getType());
                    if (byClass.isPresent()) {
                        Type<?> type = byClass.get();
                        String typeName;
                        if (parameter.isSingle()) {
                            typeName = type.getBaseName();
                        } else {
                            typeName = type.getPluralForm();
                        }
                        String format = String.format("%s:%s", parameter.getName(), typeName);
                        parameterNames.add(format);
                    }
                }


                // Create a pattern for a function
                String pattern = String.format("%s(%s)", jf.getName(), String.join(", ", parameterNames));
                functionDoc.put("patterns", new BsonArray(List.of(new BsonString(pattern))));

                // DESCRIPTION
                BsonArray descriptionArray = new BsonArray();
                for (String s : documentation.getDescription()) {
                    descriptionArray.add(new BsonString(s));
                }
                functionDoc.put("description", descriptionArray);

                // USAGE
                String usage = documentation.getUsage();
                if (usage != null) {
                    functionDoc.put("usage", new BsonString(usage));
                }

                // EXAMPLES
                String[] examples = documentation.getExamples();
                if (examples != null) {
                    BsonArray exampleArray = new BsonArray();
                    for (String s : examples) {
                        exampleArray.add(new BsonString(s));
                    }
                    functionDoc.put("examples", exampleArray);
                }

                // SINCE
                String since = documentation.getSince();
                if (since != null) {
                    functionDoc.put("since", new BsonArray(List.of(new BsonString(since))));
                } else {
                    Utils.warn(this.sender, "Function '%s' has no since tag!", name);
                }

                // RETURN TYPE
                Optional<Class<?>> returnType = (Optional<Class<?>>) jf.getReturnType();
                if (returnType.isPresent()) {
                    Optional<? extends Type<?>> byClass = TypeManager.getByClass(returnType.get());
                    byClass.ifPresent(type ->
                        functionDoc.put("return type", new BsonString(type.getBaseName())));
                }
                functionsArray.add(functionDoc);

            }
        });
        mainDocs.put("functions", functionsArray);
    }

    private void printEffects(BsonDocument mainDocs, SkriptRegistration registration) {
        BsonArray effectsArray = mainDocs.getArray("effects", new BsonArray());
        for (SyntaxInfo<? extends Effect> effect : registration.getEffects()) {
            Documentation documentation = effect.getDocumentation();
            if (documentation.isNoDoc()) continue;

            BsonDocument effectDoc = new BsonDocument();
            printDocumentation("effect", effectDoc, effect);
            effectsArray.add(effectDoc);
        }
        mainDocs.put("effects", effectsArray);
    }

    private void printTypes(BsonDocument mainDocs, SkriptRegistration registration) {
        BsonArray typesArray = mainDocs.getArray("types", new BsonArray());

        registration.getTypes().forEach(type -> {
            Documentation documentation = type.getDocumentation();
            if (documentation.isNoDoc()) return;

            BsonDocument syntaxDoc = new BsonDocument();

            // NAME and ID
            String baseName = type.getBaseName();
            String docName = documentation.getName();
            syntaxDoc.put("name", new BsonString(docName != null ? docName : baseName));
            syntaxDoc.put("id", getId("type", baseName));

            // EXPERIMENTAL
            if (documentation.isExperimental()) {
                syntaxDoc.put("experimental", new BsonString(documentation.getExperimentalMessage()));
            }

            // DESCRIPTION
            BsonArray descriptionArray = new BsonArray();
            for (String s : documentation.getDescription()) {
                descriptionArray.add(new BsonString(s));
            }
            syntaxDoc.put("description", descriptionArray);

            // USAGE
            String usage = documentation.getUsage();
            if (usage != null) {
                syntaxDoc.put("usage", new BsonString(usage));
            }

            // PATTERNS
            BsonArray patternArray = new BsonArray();
            for (String pluralForm : type.getPluralForms()) {
                patternArray.add(new BsonString(pluralForm));
            }
            syntaxDoc.put("patterns", patternArray);

            // EXAMPLES
            String[] examples = documentation.getExamples();
            if (examples != null) {
                BsonArray exampleArray = new BsonArray();
                for (String s : examples) {
                    exampleArray.add(new BsonString(s));
                }
                syntaxDoc.put("examples", exampleArray);
            }

            // SERIALIZABLE
            syntaxDoc.put("serializable", new BsonBoolean(type.getSerializer().isPresent()));

            // SINCE
            String since = documentation.getSince();
            if (since != null) {
                syntaxDoc.put("since", new BsonArray(List.of(new BsonString(since))));
            } else {
                Utils.warn(this.sender, "Type '%s' has no since tag!", baseName);
            }


            typesArray.add(syntaxDoc);

        });
        mainDocs.put("types", typesArray);
    }

    private void printSections(BsonDocument mainDocs, SkriptRegistration registration) {
        BsonArray sectionsArray = mainDocs.getArray("sections", new BsonArray());
        List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>(registration.getSections());

        for (SyntaxInfo<? extends CodeSection> section : sections) {
            Documentation documentation = section.getDocumentation();
            if (documentation.isNoDoc()) continue;

            BsonDocument sectionDoc = new BsonDocument();
            printDocumentation("section", sectionDoc, section);
            sectionsArray.add(sectionDoc);
        }
        mainDocs.put("sections", sectionsArray);
    }

    private void printDocumentation(String type, BsonDocument syntaxDoc, SyntaxInfo<?> syntaxInfo) {
        Documentation documentation = syntaxInfo.getDocumentation();

        // NAME and ID
        syntaxDoc.put("name", getName(syntaxInfo));
        syntaxDoc.put("id", getId(type, syntaxInfo));

        // EXPERIMENTAL
        if (documentation.isExperimental()) {
            syntaxDoc.put("experimental", new BsonString(documentation.getExperimentalMessage()));
        }

        // DESCRIPTION
        BsonArray descriptionArray = new BsonArray();
        for (String s : documentation.getDescription()) {
            descriptionArray.add(new BsonString(s));
        }
        syntaxDoc.put("description", descriptionArray);

        // USAGE
        String usage = documentation.getUsage();
        if (usage != null) {
            syntaxDoc.put("usage", new BsonString(usage));
        }

        // PATTERNS
        List<PatternElement> patterns = syntaxInfo.getPatterns();
        if (!patterns.isEmpty()) {
            BsonArray patternArray = new BsonArray();
            patterns.forEach(pattern -> {
                // Remove parse tags in patterns, ex: "(blah:blah|bloop:bloop)"
                String removedParseTags = pattern.toString().replaceAll("[^()\\[\\]|:\\s]*:", "");
                patternArray.add(new BsonString(removedParseTags));
            });
            syntaxDoc.put("patterns", patternArray);
        }

        // EXAMPLES
        String[] examples = documentation.getExamples();
        if (examples != null) {
            BsonArray exampleArray = new BsonArray();
            for (String s : examples) {
                exampleArray.add(new BsonString(s));
            }
            syntaxDoc.put("examples", exampleArray);
        }

        // SINCE
        String since = documentation.getSince();
        if (since != null) {
            syntaxDoc.put("since", new BsonArray(List.of(new BsonString(since))));
        } else {
            Utils.warn(this.sender, "Syntax '%s' has no since tag!", syntaxInfo.getSyntaxClass().getSimpleName());
        }
    }

    private @NotNull File getFile() {
        File file = HySk.getInstance().getDataDirectory().resolve("docs/" + this.addonKey + "/docs.json").toFile();
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    throw new RuntimeException("Unable to create docs directory.");
                } else {
                    Utils.log(this.sender, "Created docs directory.");
                }
            }
            try {
                if (!file.createNewFile()) {
                    Utils.error("Failed to create docs.json file!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private BsonString getId(String type, SyntaxInfo<?> syntaxInfo) {
        Documentation documentation = syntaxInfo.getDocumentation();
        String name = documentation.getName();
        if (name == null) name = syntaxInfo.getSyntaxClass().getSimpleName();
        return getId(type, name);
    }

    private BsonString getName(SyntaxInfo<?> syntaxInfo) {
        Documentation documentation = syntaxInfo.getDocumentation();
        String name = documentation.getName();
        if (name == null) {
            name = syntaxInfo.getSyntaxClass().getSimpleName();
        }
        return new BsonString(name);
    }

    private BsonString getId(String type, String syntaxId) {
        String addonName = this.addonKey;
        if (addonName.equalsIgnoreCase("skript-parser")) {
            // So the parser doesn't have its own docs
            addonName = "HySkript";
        }
        String s = type + ":" + addonName + ":" + syntaxId;
        return new BsonString(s.toLowerCase(Locale.ROOT).replace(" ", "_"));

    }

    private String getContextValueDescription(ContextValue<?, ?> contextValue) {
        String desc = contextValue.getDescription();
        PatternType<?> returnType = contextValue.getReturnType();

        boolean single = returnType.isSingle();
        String form = single ? "a single" : "multiple";

        String[] pluralForms = returnType.getType().getPluralForms();
        String baseName = pluralForms.length > 0 && !single ? pluralForms[1] : pluralForms[0];
        baseName = StringUtils.toCamelCase(baseName, false);

        ContextValue.State state = contextValue.getState();
        String stateName = state == ContextValue.State.PRESENT ? "" : state.name().toLowerCase(Locale.ROOT) + " ";

        boolean canBeSet = contextValue.getListSetterFunction() != null || contextValue.getSingleSetterFunction() != null;

        return String.format("`%scontext-%s`%s(Returns %s %s%s)",
            stateName, contextValue.getPattern().toString(),
            desc == null ? " " : " " + desc + " ",
            form, baseName, canBeSet ? ", can be set" : "");
    }

}
