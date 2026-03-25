package io.github.yeongseoksong.platform.jpa;

import org.springframework.data.domain.AbstractAggregateRoot;

/**
 * Aggregate root base class with domain event support.
 * Register events via registerEvent(event); they are published after repository.save().
 */
public abstract class BaseAggregateRoot<T extends BaseAggregateRoot<T>> extends AbstractAggregateRoot<T> {
}
