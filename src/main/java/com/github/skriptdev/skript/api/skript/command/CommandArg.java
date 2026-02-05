package com.github.skriptdev.skript.api.skript.command;

import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;

/**
 * Represents a command argument which contains a name, argument type, description, and optional flag.
 */
public class CommandArg {

    private final String name;
    private final String description;
    private final ArgumentType<?> type;
    private final boolean optional;

    /**
     * @param name        Name of the argument
     * @param description Description of the argument
     * @param type        Type of the argument
     * @param optional    Whether the argument is optional
     */
    private CommandArg(String name, String description, ArgumentType<?> type, boolean optional) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.optional = optional;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public ArgumentType<?> getType() {
        return this.type;
    }

    public boolean isOptional() {
        return this.optional;
    }

    @Override
    public String toString() {
        return "CommandArg{" +
            "name='" + this.name + '\'' +
            ", description='" + this.description + '\'' +
            ", type=" + this.type +
            ", optional=" + this.optional +
            '}';
    }

    /**
     * Parse a string into a CommandArg.
     *
     * @param a String to parse in the format of {@code [<name:type:desc>]} or {@code <name:type:desc>}
     * @return CommandArg
     */
    public static CommandArg parseArg(String a) {
        if (a.startsWith("[<") && a.endsWith(">]")) {
            a = a.substring(2, a.length() - 2);
            return parseArg(a, true);
        } else if (a.startsWith("<") && a.endsWith(">")) {
            a = a.substring(1, a.length() - 1);
            return parseArg(a, false);
        } else {
            return null;
        }
    }

    private static CommandArg parseArg(String a, boolean optional) {
        String name;
        String description = "";
        ArgumentType<?> type;
        if (a.contains(":")) {
            String[] split = a.split(":");
            if (split.length == 2) {
                if (split[1].startsWith("\"")) {
                    name = split[0];
                    type = ArgUtils.getType(split[0]);
                    description = split[1];
                } else {
                    name = split[0];
                    type = ArgUtils.getType(split[1]);
                }
            } else {
                name = split[0];
                type = ArgUtils.getType(split[1]);
                description = split[2];
            }
        } else {
            name = a;
            type = ArgUtils.getType(a);
        }
        if (description.startsWith("\"")) description = description.substring(1);
        if (description.endsWith("\"")) description = description.substring(0, description.length() - 1);
        if (type == null) return null;
        return new CommandArg(name, description, type, optional);
    }

}
