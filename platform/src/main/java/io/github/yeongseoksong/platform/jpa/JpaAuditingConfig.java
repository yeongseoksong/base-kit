package io.github.yeongseoksong.platform.jpa;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Activates JPA auditing (@CreatedDate, @LastModifiedDate, @CreatedBy, @LastModifiedBy).
 * To enable @CreatedBy / @LastModifiedBy, define an AuditorAware<String> bean in your application.
 */
@Configuration
@EnableJpaAuditing
@ConditionalOnClass(JpaRepository.class)
public class JpaAuditingConfig {
}
