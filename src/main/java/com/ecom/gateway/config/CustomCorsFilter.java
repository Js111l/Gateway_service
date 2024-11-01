package com.ecom.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;

@Component
public class CustomCorsFilter extends AbstractGatewayFilterFactory<CustomCorsFilter.Config> {

    public CustomCorsFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            exchange.getResponse().getHeaders().clear();
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
