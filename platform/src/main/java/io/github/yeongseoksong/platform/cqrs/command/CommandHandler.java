package io.github.yeongseoksong.platform.cqrs.command;

public interface CommandHandler<C extends Command<R>, R> {
    R handle(C command);
}
