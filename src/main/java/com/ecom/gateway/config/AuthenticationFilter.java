package com.ecom.gateway.config;

import com.ecom.gateway.clients.SecurityServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;


@Slf4j
@RefreshScope
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GatewayFilter {

    private final RouterValidator routerValidator;
    private final SecurityServiceClient client;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SESSION_ID = "sessionId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        AtomicReference<ServerHttpRequest> requestRef = new AtomicReference<>(exchange.getRequest());
        AtomicReference<ServerWebExchange> exchangeRef = new AtomicReference<>(exchange);

        try {
            final String token = this.verifyUserSession(exchange.getRequest());
            if (routerValidator.isSecured.test(requestRef.get())) {
                ServerHttpRequest mutatedRequest = requestRef.get().mutate()
                        .header(AUTHORIZATION_HEADER, token) // Attach JWT to internal service
                        .build();

                exchangeRef.set(exchangeRef.get().mutate().request(mutatedRequest).build());
            }
        } catch (Exception ex) {
            this.onError(exchangeRef.get(), HttpStatus.FORBIDDEN);
        }

        return chain.filter(exchangeRef.get());
    }


    private String verifyUserSession(ServerHttpRequest clientRequest) {
        var httpCookieList = clientRequest.getCookies().get(SESSION_ID);
        String sessionId = null;
        if (httpCookieList != null && !httpCookieList.isEmpty()) {
            sessionId = httpCookieList.getFirst().getValue();
        }
        return this.client.getJwt(sessionId).token();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

}