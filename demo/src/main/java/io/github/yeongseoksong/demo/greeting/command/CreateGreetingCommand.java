package io.github.yeongseoksong.demo.greeting.command;

import io.github.yeongseoksong.platform.cqrs.command.Command;

public record CreateGreetingCommand(String name) implements Command<String> {
}
