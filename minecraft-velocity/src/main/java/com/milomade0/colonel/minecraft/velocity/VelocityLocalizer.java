package com.milomade0.colonel.minecraft.velocity;

import com.velocitypowered.api.command.CommandSource;
import org.jetbrains.annotations.NotNull;

/**
 * Optional localization hook for Colonel's default command error messages.
 */
@FunctionalInterface
public interface VelocityLocalizer {

    void send(@NotNull CommandSource source, @NotNull String key, @NotNull Object... args);
}
