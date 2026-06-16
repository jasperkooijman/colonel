package com.milomade0.colonel.minecraft.paper;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Optional localization hook for Colonel's default command error messages.
 */
@FunctionalInterface
public interface PaperLocalizer {

    void send(@NotNull CommandSender sender, @NotNull String key, @NotNull Object... args);
}
