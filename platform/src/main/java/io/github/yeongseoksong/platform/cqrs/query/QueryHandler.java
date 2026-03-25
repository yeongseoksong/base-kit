package io.github.yeongseoksong.platform.cqrs.query;

public interface QueryHandler<Q extends Query<R>, R> {
    R handle(Q query);
}
