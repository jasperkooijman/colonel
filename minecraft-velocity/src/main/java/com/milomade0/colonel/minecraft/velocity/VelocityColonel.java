package com.milomade0.colonel.minecraft.velocity;

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
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class VelocityColonel extends MinecraftColonel<CommandSource> {

    private final ProxyServer proxyServer;
    private final Object plugin;
    private final CommandManager commandManager;
    private final Map<String, VelocityCommand> commands = new HashMap<>();

    private @Nullable BiConsumer<CommandSource, CommandFailure> errorHandler;
    private @Nullable VelocityLocalizer localizer;

    public VelocityColonel(@NotNull ProxyServer proxyServer, @NotNull Object plugin) {
        super(CommandSource.class);

        this.proxyServer = proxyServer;
        this.plugin = plugin;
        this.commandManager = proxyServer.getCommandManager();

        registry().registerSourceMapper(Player.class, source -> {
            if (source instanceof Player player) {
                return player;
            }
            throw FailureHandler.of(() -> sendMessage(source, "cmderr.sender-not-player",
                    "This command can only be executed by a player."));
        });
    }

    public VelocityColonel(@NotNull ProxyServer proxyServer, @NotNull Object plugin, @NotNull VelocityLocalizer localizer) {
        this(proxyServer, plugin);
        this.localizer = localizer;
    }

    @Override
    public void register(@NotNull CommandHandler handler, @NotNull String... paths) {
        super.register(handler, paths);

        for (String path : paths) {
            String firstLiteral = path.split(" ")[0];
            if (commands.containsKey(firstLiteral)) {
                continue;
            }

            VelocityCommand command = new VelocityCommand(this);
            CommandMeta meta = commandManager.metaBuilder(firstLiteral)
                    .plugin(plugin)
                    .build();
            commandManager.register(meta, command);
            commands.put(firstLiteral, command);
        }
    }

    @Override
    protected Audience audience(CommandSource source) {
        return source;
    }

    @Override
    protected void buildCommand(@NotNull Method method, @NotNull Map<Parameter, Function<SafeCommandContext<CommandSource>, Object>> suppliers, @NotNull SafeCommandHandlerBuilder<CommandSource> builder) {
        super.buildCommand(method, suppliers, builder);

        Permission permissionConf = method.getAnnotation(Permission.class);
        if (permissionConf != null) {
            builder.condition(source -> source.hasPermission(replacePlaceholders(permissionConf.value())));
        }
    }

    @Override
    public void dispatch(CommandSource source, String input) {
        try {
            super.dispatch(source, input);
        } catch (CommandFailure failure) {
            handle(source, failure);
        }
    }

    @Override
    public List<Suggestion> suggestions(CommandSource source, String input, int cursor) {
        try {
            return super.suggestions(source, input, cursor);
        } catch (CommandFailure failure) {
            handle(source, failure);
        }
        return List.of();
    }

    public ProxyServer proxyServer() {
        return proxyServer;
    }

    public void setErrorHandler(@Nullable BiConsumer<CommandSource, CommandFailure> errorHandler) {
        this.errorHandler = errorHandler;
    }

    public void setLocalizer(@Nullable VelocityLocalizer localizer) {
        this.localizer = localizer;
    }

    private void handle(CommandSource source, CommandFailure failure) {
        if (errorHandler != null) {
            errorHandler.accept(source, failure);
            return;
        }

        if (failure instanceof CommandNotFoundFailure) {
            sendMessage(source, "cmderr.command-not-found", "Command not found: {0}", failure.command());
            return;
        }

        if (failure instanceof CommandPrepareParameterFailure prepareFailure) {
            if (prepareFailure.input() == null) {
                sendMessage(source, "cmderr.parameter-is-missing",
                        "The parameter {0} is missing. Expected syntax: {1}.",
                        prepareFailure.parameter().name(),
                        prepareFailure.path() + " " + prepareFailure.definition());
                return;
            }
            if (prepareFailure.getCause() instanceof IllegalArgumentException) {
                sendMessage(source, "cmderr.parameter-invalid-value",
                        "The value {0} is invalid for parameter {1}.",
                        prepareFailure.input(),
                        prepareFailure.parameter().name());
                return;
            }
        }

        sendMessage(source, "cmderr.generic", "An unexpected error occurred, check the console for more information.");

        if (failure.getCause() != null) {
            failure.getCause().printStackTrace();
        }
    }

    void sendMessage(CommandSource source, String key, String fallback, Object... args) {
        if (localizer != null) {
            localizer.send(source, key, args);
            return;
        }

        String message = fallback;
        for (int index = 0; index < args.length; index++) {
            message = message.replace("{" + index + "}", String.valueOf(args[index]));
        }
        source.sendMessage(Component.text(message));
    }
}
