package com.milomade0.colonel.minecraft.paper;

import com.milomade0.colonel.common.dispatch.suggestion.Suggestion;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PaperCommand extends Command {

    private final PaperColonel colonel;

    public PaperCommand(@NotNull PaperColonel colonel, @NotNull String firstLiteral) {
        super(firstLiteral);
        this.colonel = colonel;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        String input = args.length == 0 ? getName() : getName() + " " + String.join(" ", args);
        colonel.dispatch(sender, input);
        return true;
    }

    @Override
    public boolean testPermissionSilent(@NotNull CommandSender target) {
        return colonel.available(target, getName());
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        if (!testPermissionSilent(sender)) {
            return List.of();
        }
        String input = args.length == 0 ? getName() + " " : getName() + " " + String.join(" ", args);
        return colonel.suggestions(sender, input).stream().map(Suggestion::value).toList();
    }
}
