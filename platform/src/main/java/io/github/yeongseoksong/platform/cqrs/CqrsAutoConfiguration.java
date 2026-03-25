package io.github.yeongseoksong.platform.cqrs;

import io.github.yeongseoksong.platform.cqrs.bus.SimpleCommandBus;
import io.github.yeongseoksong.platform.cqrs.bus.SimpleQueryBus;
import io.github.yeongseoksong.platform.cqrs.command.CommandBus;
import io.github.yeongseoksong.platform.cqrs.command.CommandHandler;
import io.github.yeongseoksong.platform.cqrs.query.QueryBus;
import io.github.yeongseoksong.platform.cqrs.query.QueryHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

import java.util.List;

@AutoConfiguration
public class CqrsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CommandBus.class)
    public CommandBus commandBus(List<CommandHandler<?, ?>> handlers) {
        return new SimpleCommandBus(handlers);
    }

    @Bean
    @ConditionalOnMissingBean(QueryBus.class)
    public QueryBus queryBus(List<QueryHandler<?, ?>> handlers) {
        return new SimpleQueryBus(handlers);
    }
}
