package com.github.skriptdev.skript.api.skript.docs;

import com.github.skriptdev.skript.api.skript.event.CancellableContext;
import com.github.skriptdev.skript.api.utils.Utils;
import com.github.skriptdev.skript.plugin.HySk;
import com.github.skriptdev.skript.plugin.Skript;
import io.github.syst3ms.skriptparser.Parser;
import io.github.syst3ms.skriptparser.docs.Documentation;
import io.github.syst3ms.skriptparser.lang.CodeSection;
import io.github.syst3ms.skriptparser.lang.Effect;
import io.github.syst3ms.skriptparser.lang.Structure;
import io.github.syst3ms.skriptparser.lang.base.ConditionalExpression;
import io.github.syst3ms.skriptparser.lang.base.ExecutableExpression;
import io.github.syst3ms.skriptparser.pattern.PatternElement;
import io.github.syst3ms.skriptparser.pattern.TextElement;
import io.github.syst3ms.skriptparser.registration.ExpressionInfo;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Documentation printer.
 */
public class MarkdownDocPrinter {

    /**
     * Print all docs to file.
     */
    public static void printDocs() {
        Skript skript = HySk.getInstance().getSkript();
        SkriptRegistration registration = skript.getSkriptRegistration();

        try {
            Utils.log("Printing documentation");

            // EXPRESSIONS
            Utils.log("Printing expressions and conditions");
            File file = getFile("expressions");
            PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
            PrintWriter condWriter = new PrintWriter(getFile("conditions"), StandardCharsets.UTF_8);
            PrintWriter effCondWriter = new PrintWriter(getFile("effect-expressions"), StandardCharsets.UTF_8);
            printExpressions(writer, condWriter, effCondWriter, registration);
            printExpressions(writer, condWriter, effCondWriter, Parser.getMainRegistration());

            condWriter.close();
            writer.close();
            effCondWriter.close();

            // EFFECTS
            Utils.log("Printing effects");
            file = getFile("effects");

            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printEffects(writer, registration);
            printEffects(writer, Parser.getMainRegistration());

            writer.close();

            // EVENTS
            Utils.log("Printing events");
            file = getFile("events");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printEvents(writer, registration);
            printEvents(writer, Parser.getMainRegistration());
            writer.close();

            // STRUCTURES
            Utils.log("Printing structures");
            file = getFile("structures");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printStructures(writer, registration);
            printStructures(writer, Parser.getMainRegistration());
            writer.close();

            // TYPES
            Utils.log("Printing types");
            file = getFile("types");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printTypes(writer, registration);
            printTypes(writer, Parser.getMainRegistration());
            writer.close();

            // SECTIONS
            Utils.log("Printing sections");
            file = getFile("sections");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printSections(writer, registration);
            printSections(writer, Parser.getMainRegistration());
            writer.close();

            // FUNCTIONS
            Utils.log("Printing functions");
            file = getFile("functions");
            writer = new PrintWriter(file, StandardCharsets.UTF_8);
            printFunctions(writer, registration);
            printFunctions(writer, Parser.getMainRegistration());
            writer.close();

            Utils.log("Documentation printed!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printEvents(PrintWriter writer, SkriptRegistration registration) {
        List<ContextValue<?, ?>> contextValues = registration.getContextValues();

        registration.getEvents().forEach(event -> {
            Documentation documentation = event.getDocumentation();
            if (documentation.isNoDoc()) return;

            if (!Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                printDocumentation("Event", writer, documentation, event.getPatterns());
            }

            AtomicBoolean cancellable = new AtomicBoolean(false);
            event.getContexts().forEach(context -> {
                if (CancellableContext.class.isAssignableFrom(context)) {
                    cancellable.set(true);
                }
            });
            writer.println("- **Cancellable**: " + cancellable.get());

            List<ContextValue<?, ?>> valuesForThisEvent = new ArrayList<>();
            contextValues.forEach(contextValue -> {
                event.getContexts().forEach(context -> {
                    if (contextValue.getContext().isAssignableFrom(context)) {
                        valuesForThisEvent.add(contextValue);
                    }
                });
            });
            if (!valuesForThisEvent.isEmpty()) {
                writer.println("- **ContextValues**:");
                valuesForThisEvent.forEach(contextValue -> {
                    PatternElement pattern = contextValue.getPattern();
                    PatternType<?> returnType = contextValue.getReturnType();
                    boolean single = returnType.isSingle();
                    String c = single ? "a single" : "multiple";
                    String baseName = getLinkForType(returnType.getType(), single);
                    writer.println("   - `context-" + pattern + "` returns " + c + " " + baseName);
                });
            }
        });
    }

    private static void printStructures(PrintWriter writer, SkriptRegistration registration) {
        registration.getEvents().forEach(event -> {
            Documentation documentation = event.getDocumentation();
            if (documentation.isNoDoc()) return;

            if (Structure.class.isAssignableFrom(event.getSyntaxClass())) {
                printDocumentation("Structure", writer, documentation, event.getPatterns());
            }
        });
    }

    private static void printExpressions(PrintWriter exprWriter, PrintWriter condWriter,
                                         PrintWriter exprEffWriter, SkriptRegistration registration) {
        List<List<ExpressionInfo<?, ?>>> values = new ArrayList<>(registration.getExpressions().values());
        values.sort(Comparator.comparing(k -> k.getFirst().getSyntaxClass().getSimpleName()));

        for (List<ExpressionInfo<?, ?>> expressionInfos : values) {
            expressionInfos.forEach(expressionInfo -> {
                Documentation documentation = expressionInfo.getDocumentation();
                if (documentation.isNoDoc()) return;

                String returnType = getLinkForType(expressionInfo.getReturnType().getType(), expressionInfo.getReturnType().isSingle());
                Class<?> syntaxClass = expressionInfo.getSyntaxClass();
                if (ExecutableExpression.class.isAssignableFrom(syntaxClass)) {
                    printDocumentation("Effect Expression", exprEffWriter, documentation, expressionInfo.getPatterns());
                    condWriter.println("- **Return Type**: " + returnType);
                } else if (ConditionalExpression.class.isAssignableFrom(syntaxClass)) {
                    printDocumentation("Condition", condWriter, documentation, expressionInfo.getPatterns());
                    condWriter.println("- **Return Type**: " + returnType);
                } else {
                    printDocumentation("Expression", exprWriter, documentation, expressionInfo.getPatterns());
                    exprWriter.println("- **Return Type**: " + returnType);
                }
            });
            exprWriter.println();
        }
    }

    @SuppressWarnings("unchecked")
    private static void printFunctions(PrintWriter writer, SkriptRegistration reg) {
        Functions.getJavaFunctions(reg).stream().sorted(Comparator.comparing(Function::getName)).forEach(function -> {
            if (function instanceof JavaFunction<?> jf) {
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
                Documentation documentation = jf.getDocumentation();
                if (documentation.isNoDoc()) return;

                // Create a pattern for a function
                String pattern = String.format("%s(%s)", jf.getName(), String.join(", ", parameterNames));
                TextElement textElement = new TextElement(pattern);

                printDocumentation("Function", writer, documentation, List.of(textElement));
                Optional<Class<?>> returnType = (Optional<Class<?>>) jf.getReturnType();
                if (returnType.isPresent()) {
                    Optional<? extends Type<?>> byClass = TypeManager.getByClass(returnType.get());
                    if (byClass.isPresent()) {
                        String t = getLinkForType(byClass.get(), jf.isReturnSingle());
                        writer.println("- **Return Type**: " + t);
                    }
                }

            }
        });
    }

    private static void printEffects(PrintWriter writer, SkriptRegistration registration) {
        for (SyntaxInfo<? extends Effect> effect : registration.getEffects()) {
            Documentation documentation = effect.getDocumentation();
            if (documentation.isNoDoc()) continue;

            printDocumentation("Effect", writer, documentation, effect.getPatterns());
        }
    }

    private static void printTypes(PrintWriter writer, SkriptRegistration registration) {
        List<Type<?>> types = new ArrayList<>(registration.getTypes());
        types.sort(Comparator.comparing(Type::getBaseName));
        types.forEach(type -> {
            Documentation documentation = type.getDocumentation();
            if (documentation.isNoDoc()) return;

            printDocumentation("Type", writer, documentation, List.of());
            writer.println("- **Can Be Serialized**: " + type.getSerializer().isPresent());
        });
    }

    private static void printSections(PrintWriter writer, SkriptRegistration registration) {
        List<SyntaxInfo<? extends CodeSection>> sections = new ArrayList<>(registration.getSections());
        sections.sort(Comparator.comparing(info -> info.getSyntaxClass().getSimpleName()));
        for (SyntaxInfo<? extends CodeSection> section : sections) {
            Documentation documentation = section.getDocumentation();
            if (documentation.isNoDoc()) continue;

            printDocumentation("Section", writer, documentation, section.getPatterns());
        }
    }

    private static void printDocumentation(String type, PrintWriter writer, Documentation documentation, List<PatternElement> patterns) {
        writer.println("## " + documentation.getName());
        if (documentation.isExperimental()) {
            writer.println("> [!WARNING]");
            writer.println("> **This is an experimental feature!**  ");
            writer.println("> " + documentation.getExperimentalMessage());
        }
        String[] description = documentation.getDescription();
        if (description.length > 0) {
            writer.println("- **Description**:");
            for (String s : description) {
                if (s.contains("\n")) {
                    for (String string : s.split("\\n")) {
                        writer.println("  " + string + "  ");
                    }
                } else {
                    writer.println("  " + s + "  ");
                }
            }
        }
        if (documentation.getUsage() != null) {
            writer.println("- **Usage**:  ");
            String usage = documentation.getUsage();
            if (usage.length() > 200) {
                // Asset store stuff gets really long, so plop them on another page
                // GitHub's wiki pages seem to have a limit
                Utils.log("Creating asset store link for: " + documentation.getName());
                writer.println("[Click Here](https://github.com/SkriptDev/HySkript/wiki/_usage-" +
                    documentation.getName().replace(" ", "-") + ")");
                File usageFile = getFile("_usage-" + documentation.getName());
                try {
                    List<String> values = new ArrayList<>();
                    List<String> variations = new ArrayList<>();
                    for (String s : usage.split(", ")) {
                        if (s.startsWith("*")) {
                            variations.add(s);
                        } else {
                            values.add(s);
                        }
                    }
                    boolean hasVariations = !variations.isEmpty();
                    PrintWriter usageWriter = new PrintWriter(usageFile, StandardCharsets.UTF_8);
                    usageWriter.println("# Usage: " + documentation.getName());

                    usageWriter.println("<details>");
                    usageWriter.println("<summary>Values</summary>");
                    usageWriter.println();
                    usageWriter.println("```yaml"); // Yaml makes it a nicer blue
                    for (String s : values.stream().sorted().toList()) {
                        usageWriter.println(s);
                    }
                    usageWriter.println("```");
                    usageWriter.println();
                    usageWriter.println("</details>");
                    usageWriter.println();

                    if (hasVariations) {
                        usageWriter.println("<details>");
                        usageWriter.println("<summary>Variations</summary>");
                        usageWriter.println();
                        usageWriter.println("```yaml"); // Yaml makes it a nicer blue
                        for (String s : variations.stream().sorted().toList()) {
                            usageWriter.println(s);
                        }
                        usageWriter.println("```");
                        usageWriter.println();
                        usageWriter.println("</details>");
                        usageWriter.println();
                    }

                    usageWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                writer.println("`");
                writer.println(documentation.getUsage());
                writer.println("`");
            }
        }
        if (!patterns.isEmpty()) {
            if (patterns.size() == 1) {
                writer.println("- **Pattern**: `" + patterns.getFirst() + "`");
            } else {
                writer.println("- **Patterns**:");
                patterns.forEach(pattern -> {
                    String r = pattern.toString().replaceAll("-?\\w+:", "");
                    writer.println("   - `" + r + "`");
                });
            }
        }
        String[] examples = documentation.getExamples();
        if (examples.length > 0) {
            writer.println("- **Examples**:  ");
            writer.println("```applescript");
            for (String s : examples) {
                writer.println(s);
            }
            writer.println("```");
        }
        if (documentation.getSince() != null) {
            writer.println("- **Since**: " + documentation.getSince());
        }
    }

    static private @NotNull File getFile(String name) {
        File file = HySk.getInstance().getDataDirectory().resolve("docs/markdown-docs/" + name + ".md").toFile();
        if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    throw new RuntimeException("Unable to create docs directory.");
                } else {
                    Utils.log("Created docs directory.");
                }
            }
            try {
                if (!file.createNewFile()) {
                    Utils.error("Failed to create " + name + ".md file!");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return file;
    }

    private static String getLinkForType(Type<?> type, boolean single) {
        String[] pluralForms = type.getPluralForms();
        String baseName = pluralForms.length > 0 && !single ? pluralForms[1] : pluralForms[0];
        String name = type.getDocumentation().getName().replace(" ", "-").toLowerCase(Locale.ROOT);
        return String.format("[%s](https://github.com/SkriptDev/HySkript/wiki/types#%s)", baseName, name);
    }

}
