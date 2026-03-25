package io.github.yeongseoksong.platform.datasource;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "base-kit.datasource")
public class BasekitDatasourceProperties {

    private boolean enabled = false;
    private DataSourceDetail write = new DataSourceDetail();
    private DataSourceDetail read = new DataSourceDetail();

    @Getter
    @Setter
    public static class DataSourceDetail {
        private String url;
        private String username;
        private String password;
        private String driverClassName;
        private Pool pool = new Pool();

        @Getter
        @Setter
        public static class Pool {
            private int maximumPoolSize = 10;
            private int minimumIdle = 10;
            private long connectionTimeout = 30000;
            private long idleTimeout = 600000;
            private long maxLifetime = 1800000;
        }
    }
}
