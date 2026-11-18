package br.com.wti.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

@Configuration
public class AntPathMatcherConfig {

    @Bean
    AntPathMatcher antPathMatcher() {
        return new AntPathMatcher();
    }
}
