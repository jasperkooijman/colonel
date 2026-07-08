package com.milomade0.colonel.minecraft.velocity;

import com.milomade0.colonel.common.dispatch.suggestion.Suggestion;
import com.velocitypowered.api.command.SimpleCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class VelocityCommand implements SimpleCommand {

    private final VelocityColonel colonel;

    public VelocityCommand(@NotNull VelocityColonel colonel) {
        this.colonel = colonel;
    }

    @Override
    public void execute(Invocation invocation) {
        colonel.dispatch(invocation.source(), input(invocation));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return colonel.suggestions(invocation.source(), input(invocation)).stream()
                .map(Suggestion::value)
                .toList();
    }

    private String input(Invocation invocation) {
        String[] arguments = invocation.arguments();
        return arguments.length == 0 ? invocation.alias() : invocation.alias() + " " + String.join(" ", arguments);
    }
}
