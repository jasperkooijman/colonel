package com.milomade0.colonel.minecraft.paper;

import com.milomade0.colonel.minecraft.paper.PaperLocalizer;
import com.milomade0.colonel.common.build.FailureHandler;
import com.milomade0.colonel.common.dispatch.suggestion.Suggestion;
import com.milomade0.colonel.common.dispatch.tree.CommandHandler;
import com.milomade0.colonel.common.exception.CommandFailure;
import com.milomade0.colonel.common.exception.CommandNotFoundFailure;
import com.milomade0.colonel.common.exception.CommandPrepareParameterFailure;
import com.milomade0.colonel.common.safe.SafeCommandContext;
import com.milomade0.colonel.common.safe.SafeCommandHandlerBuilder;
import com.milomade0.colonel.minecraft.common.MinecraftColonel;
import com.milomade0.colonel.minecraft.common.annotations.Permission;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class PaperColonel extends MinecraftColonel<CommandSender> {

    final JavaPlugin plugin;
    final BukkitAudiences audiences;

    private final SimpleCommandMap commandMap;
    private BiConsumer<CommandSender, CommandFailure> errorHandler;

    record RegisteredCommand(@NotNull String path, @NotNull CommandHandler handler, @NotNull PaperCommand command) {
    }

    final Set<RegisteredCommand> commands = new HashSet<>();

    private @Nullable PaperLocalizer localizer;

    public PaperColonel(@NotNull JavaPlugin plugin) {
        super(CommandSender.class);

        this.plugin = plugin;
        this.audiences = BukkitAudiences.create(plugin);

        registry().registerSourceMapper(Player.class, cs -> {
            if (cs instanceof Player p) {
                return p;
            }
            throw FailureHandler.of(() -> sendMessage(cs, "cmderr.sender-not-player",
                    ChatColor.RED + "This command can only be executed by a player."));
        });

        try {
            commandMap = (SimpleCommandMap) plugin.getServer().getClass()
                    .getMethod("getCommandMap").invoke(plugin.getServer());
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        registerAll(new PaperArguments(this));
    }

    public PaperColonel(@NotNull JavaPlugin plugin, @NotNull PaperLocalizer localizer) {
        this(plugin);
        this.localizer = localizer;
    }

    @Override
    public void register(@NotNull CommandHandler handler, @NotNull String... paths) {
        super.register(handler, paths);

        for ( String path : paths ) {
            String firstLiteral = path.split(" ")[0];
            PaperCommand cmd = commands.stream()
                    .filter(c -> (c.path() + " ").startsWith(firstLiteral + " "))
                    .map(c -> c.command)
                    .findFirst().orElse(null);

            if (cmd == null) {
                cmd = new PaperCommand(this, firstLiteral);
                commandMap.register(plugin.getName().toLowerCase(), cmd);
            }

            commands.add(new RegisteredCommand(path, handler, cmd));
        }
    }

    @Override
    protected Audience audience(CommandSender source) {
        return audiences.sender(source);
    }

    @Override
    protected void buildCommand(@NotNull Method method, @NotNull Map<Parameter, Function<SafeCommandContext<CommandSender>, Object>> suppliers, @NotNull SafeCommandHandlerBuilder<CommandSender> builder) {
        super.buildCommand(method, suppliers, builder);

        Permission permissionConf = method.getAnnotation(Permission.class);
        if (permissionConf != null) {
            builder.condition(s -> s.hasPermission(replacePlaceholders(permissionConf.value())));
        }
    }

    @Override
    public void dispatch(CommandSender source, String input) {
        try {
            super.dispatch(source, input);
        } catch (CommandFailure failure) {
            handle(source, failure);
        }
    }

    @Override
    public List<Suggestion> suggestions(CommandSender source, String input, int cursor) {
        try {
            return super.suggestions(source, input, cursor);
        } catch (CommandFailure failure) {
            handle(source, failure);
        }
        return List.of();
    }

    //

    public void setErrorHandler(BiConsumer<CommandSender, CommandFailure> errorHandler) {
        this.errorHandler = errorHandler;
    }

    //

    private void handle(CommandSender source, CommandFailure failure) {
        if (errorHandler != null) {
            errorHandler.accept(source, failure);
            return;
        }

        // USER FACING ERRORS

        if (failure instanceof CommandNotFoundFailure) {
            sendMessage(source, "cmderr.command-not-found",
                    ChatColor.RED + "Command not found: " + ChatColor.DARK_RED + "{0}", failure.command());
            return;
        }

        if (failure instanceof CommandPrepareParameterFailure pf) {
            if (pf.input() == null) {
                sendMessage(source, "cmderr.parameter-is-missing",
                        ChatColor.RED + "The parameter " + ChatColor.DARK_RED + "{0}" +
                                ChatColor.RED + " is missing. Expected syntax: " + ChatColor.DARK_RED + "{1}" +
                                ChatColor.RED + ".", pf.parameter().name(), pf.path() + " " + pf.definition().toString());
                return;
            }
            if (pf.getCause() instanceof IllegalArgumentException) {
                sendMessage(source, "cmderr.parameter-invalid-value",
                        ChatColor.RED + "The value " + ChatColor.DARK_RED + "{0}" +
                                ChatColor.RED + " is invalid for parameter " + ChatColor.DARK_RED + "{1}" +
                                ChatColor.RED + ".", pf.input(), pf.parameter().name());
                return;
            }
        }

        // INTERNAL ERRORS FOR THE DEVELOPER

        sendMessage(source, "cmderr.generic", ChatColor.RED + "An unexpected error occured, check the console for more information.");

        if (failure.getCause() != null) {
            failure.getCause().printStackTrace();
        }
    }

    //

    void sendMessage(CommandSender source, String i18n, String fallback, Object... args) {
        if (this.localizer != null) {
            localizer.send(source, i18n, args);
            return;
        }

        String str = fallback;
        for (int i = 0; i < args.length; i++) {
            str = str.replace("{" + i + "}", args[i].toString());
        }
        source.sendMessage(str);
    }
}
