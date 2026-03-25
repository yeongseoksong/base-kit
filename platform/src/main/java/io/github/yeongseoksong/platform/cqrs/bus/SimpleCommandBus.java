package io.github.yeongseoksong.platform.cqrs.bus;

import io.github.yeongseoksong.platform.cqrs.command.Command;
import io.github.yeongseoksong.platform.cqrs.command.CommandBus;
import io.github.yeongseoksong.platform.cqrs.command.CommandHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleCommandBus implements CommandBus {

    private final Map<Class<?>, CommandHandler<?, ?>> handlers = new HashMap<>();

    public SimpleCommandBus(List<CommandHandler<?, ?>> handlerList) {
        for (CommandHandler<?, ?> handler : handlerList) {
            Class<?> commandType = resolveCommandType(handler);
            if (commandType == null) {
                throw new IllegalStateException(
                        "Cannot resolve command type for handler: " + handler.getClass().getSimpleName());
            }
            handlers.put(commandType, handler);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R dispatch(Command<R> command) {
        CommandHandler<Command<R>, R> handler =
                (CommandHandler<Command<R>, R>) handlers.get(command.getClass());
        if (handler == null) {
            throw new IllegalStateException(
                    "No handler found for command: " + command.getClass().getSimpleName());
        }
        return handler.handle(command);
    }

    private Class<?> resolveCommandType(CommandHandler<?, ?> handler) {
        Class<?> targetClass = AopUtils.getTargetClass(handler);
        return ResolvableType.forClass(targetClass)
                .as(CommandHandler.class)
                .getGeneric(0)
                .resolve();
    }
}
