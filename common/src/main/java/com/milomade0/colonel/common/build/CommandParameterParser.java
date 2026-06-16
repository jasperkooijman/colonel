package com.milomade0.colonel.common.build;

@FunctionalInterface
public interface CommandParameterParser {

    Object parse(CommandContext context, String input) throws Throwable;

}
