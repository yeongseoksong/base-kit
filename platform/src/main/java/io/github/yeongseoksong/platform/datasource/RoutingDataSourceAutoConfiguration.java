package io.github.yeongseoksong.platform.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.yeongseoksong.platform.datasource.BasekitDatasourceProperties.DataSourceDetail;
import io.github.yeongseoksong.platform.datasource.BasekitDatasourceProperties.DataSourceDetail.Pool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.util.Map;

@AutoConfiguration
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "base-kit.datasource", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(BasekitDatasourceProperties.class)
public class RoutingDataSourceAutoConfiguration {

    @Bean
    public DataSource writeDataSource(BasekitDatasourceProperties properties) {
        return buildHikari("write", properties.getWrite());
    }

    @Bean
    public DataSource readDataSource(BasekitDatasourceProperties properties) {
        return buildHikari("read", properties.getRead());
    }

    @Bean
    @Primary
    public DataSource dataSource(@Qualifier("writeDataSource") DataSource writeDataSource, @Qualifier("readDataSource")DataSource readDataSource) {
        RoutingDataSource routing = new RoutingDataSource();
        routing.setTargetDataSources(Map.of(
                DataSourceType.WRITE, writeDataSource,
                DataSourceType.READ,  readDataSource
        ));
        routing.setDefaultTargetDataSource(writeDataSource);
        routing.afterPropertiesSet();
        return new LazyConnectionDataSourceProxy(routing);
    }

    private DataSource buildHikari(String poolName, DataSourceDetail detail) {
        Pool pool = detail.getPool();

        HikariConfig config = new HikariConfig();
        config.setPoolName("HikariPool-" + poolName);
        config.setJdbcUrl(detail.getUrl());
        config.setUsername(detail.getUsername());
        config.setPassword(detail.getPassword());
        config.setDriverClassName(detail.getDriverClassName());
        config.setMaximumPoolSize(pool.getMaximumPoolSize());
        config.setMinimumIdle(pool.getMinimumIdle());
        config.setConnectionTimeout(pool.getConnectionTimeout());
        config.setIdleTimeout(pool.getIdleTimeout());
        config.setMaxLifetime(pool.getMaxLifetime());

        return new HikariDataSource(config);
    }
}
