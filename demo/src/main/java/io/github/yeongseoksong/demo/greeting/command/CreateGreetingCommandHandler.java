package io.github.yeongseoksong.demo.greeting.command;

import io.github.yeongseoksong.platform.cqrs.command.CommandHandler;
import org.springframework.stereotype.Component;

@Component
public class CreateGreetingCommandHandler implements CommandHandler<CreateGreetingCommand, String> {

    @Override
    public String handle(CreateGreetingCommand command) {
        // 실제 서비스라면 도메인 객체 생성 후 저장
        return "Hello, " + command.name() + "!";
    }
}
