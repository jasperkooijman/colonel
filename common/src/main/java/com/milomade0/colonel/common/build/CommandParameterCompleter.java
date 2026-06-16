package com.milomade0.colonel.common.build;

import com.milomade0.colonel.common.dispatch.suggestion.Suggestion;

import java.util.List;

@FunctionalInterface
public interface CommandParameterCompleter {

    List<Suggestion> suggestions(CommandContext context, String input) throws Throwable;

}
