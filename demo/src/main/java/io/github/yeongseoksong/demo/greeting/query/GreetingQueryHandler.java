package io.github.yeongseoksong.demo.greeting.query;

import io.github.yeongseoksong.platform.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class GreetingQueryHandler implements QueryHandler<GreetingQuery, String> {

    @Override
    public String handle(GreetingQuery query) {
        // 실제 서비스라면 DB 조회
        return "Hi, " + query.name() + "!";
    }
}
