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

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        AtomicReference<ServerHttpRequest> requestRef = new AtomicReference<>(exchange.getRequest());
        AtomicReference<ServerWebExchange> exchangeRef = new AtomicReference<>(exchange);

        try {
            var token = this.verifyUserSession(exchange.getRequest());
            if (routerValidator.isSecured.test(requestRef.get())) {
                ServerHttpRequest mutatedRequest = requestRef.get().mutate()
                        .header("Authorization", token) // Attach JWT to internal service
                        .build();

                exchangeRef.set(exchangeRef.get().mutate().request(mutatedRequest).build());
            }
        } catch (Exception ex) {
            this.onError(exchangeRef.get(), HttpStatus.FORBIDDEN);
        }

        return chain.filter(exchangeRef.get());//); // Proceed only after successful verification
    }


    private String verifyUserSession(ServerHttpRequest clientRequest) {
        //wyslanie req do auth service i sprawdzenie czy sesja jest poprawna?
        //1. idzie request z clienta do jakiegos serwisu.
        //2. sprawdzamy czy session id w cookies sa poprawne
        //3. jesli tak to generujemy jwt i wysylamy req z jwt do mikroserwisu
        //4. mikroserwis waliduje jwt i wysyla odpowiedz, a gateway ja przekazuje klientowi.
        //5. kiedy klient robi kolejnego requesta, schemat jest podobny. Tokeny jwt nie sa nigdzie zapisywane, sa jednorazowe i bezstanowe, jako dowod ze
        // request jest napewno autoryzowany.

        var httpCookieList = clientRequest.getCookies().get("sessionId");
        String sessionId = null;

        if (httpCookieList != null) {
            sessionId = httpCookieList.get(0).getValue();
        } else {
             //throw new RuntimeException("NO SESSIONID");
        }

        return this.client.getJwt(sessionId).token();
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    private String getAuthHeader(ServerHttpRequest request) {
        return request.getHeaders().getOrEmpty("Authorization").get(0);
    }

    private boolean isAuthMissing(ServerHttpRequest request) {
        return !request.getHeaders().containsKey("Authorization");
    }

}