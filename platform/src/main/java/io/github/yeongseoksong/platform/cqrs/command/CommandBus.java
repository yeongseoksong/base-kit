package io.github.yeongseoksong.platform.cqrs.command;

public interface CommandBus {
    <R> R dispatch(Command<R> command);
}
