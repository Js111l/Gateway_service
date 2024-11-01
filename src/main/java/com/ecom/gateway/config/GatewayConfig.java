package com.ecom.gateway.config;

import lombok.AllArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@AllArgsConstructor
public class GatewayConfig {

    private final CustomCorsFilter filter;
    private final AuthenticationFilter authenticationFilter;


    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("http://localhost:8080")
                )
                .route("financial-transactions-service", r -> r.path("/payments/**")
                        .filters(f ->
                                        f.filter(authenticationFilter)
                                        .filter(filter.apply(new CustomCorsFilter.Config()))
                        )
                        .uri("http://localhost:8082")

                ).route("product-service", r -> r.path("/products/**")
                        .filters(f -> f.filter(authenticationFilter))
                        .uri("http://localhost:8084")

                )
                .build();
    }


}