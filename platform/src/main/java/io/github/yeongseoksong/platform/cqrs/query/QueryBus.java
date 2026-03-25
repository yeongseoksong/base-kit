package io.github.yeongseoksong.platform.cqrs.query;

public interface QueryBus {
    <R> R dispatch(Query<R> query);
}
