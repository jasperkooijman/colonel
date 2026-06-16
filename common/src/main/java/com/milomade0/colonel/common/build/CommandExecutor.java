package com.milomade0.colonel.common.build;

@FunctionalInterface
public interface CommandExecutor {

    void execute(CommandContext context) throws Throwable;

}
