package org.socialmedia.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Data
public class AppProperties {
    private String jwtSecret;
    private long jwtExpirationMs;
}