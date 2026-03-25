package io.github.yeongseoksong.platform.cqrs.bus;

import io.github.yeongseoksong.platform.cqrs.query.Query;
import io.github.yeongseoksong.platform.cqrs.query.QueryBus;
import io.github.yeongseoksong.platform.cqrs.query.QueryHandler;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleQueryBus implements QueryBus {

    private final Map<Class<?>, QueryHandler<?, ?>> handlers = new HashMap<>();

    public SimpleQueryBus(List<QueryHandler<?, ?>> handlerList) {
        for (QueryHandler<?, ?> handler : handlerList) {
            Class<?> queryType = resolveQueryType(handler);
            if (queryType == null) {
                throw new IllegalStateException(
                        "Cannot resolve query type for handler: " + handler.getClass().getSimpleName());
            }
            handlers.put(queryType, handler);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R dispatch(Query<R> query) {
        QueryHandler<Query<R>, R> handler =
                (QueryHandler<Query<R>, R>) handlers.get(query.getClass());
        if (handler == null) {
            throw new IllegalStateException(
                    "No handler found for query: " + query.getClass().getSimpleName());
        }
        return handler.handle(query);
    }

    private Class<?> resolveQueryType(QueryHandler<?, ?> handler) {
        Class<?> targetClass = AopUtils.getTargetClass(handler);
        return ResolvableType.forClass(targetClass)
                .as(QueryHandler.class)
                .getGeneric(0)
                .resolve();
    }
}
