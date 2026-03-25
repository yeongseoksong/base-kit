package io.github.yeongseoksong.demo.greeting.query;

import io.github.yeongseoksong.platform.cqrs.query.Query;

public record GreetingQuery(String name) implements Query<String> {
}
