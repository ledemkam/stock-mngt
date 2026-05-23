package com.kte.backend.properties;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String privateKeyPath;
    private String publicKeyPath;
    private Duration accessTokenExpiration; // ex: 15m, 1h, 1d
}
