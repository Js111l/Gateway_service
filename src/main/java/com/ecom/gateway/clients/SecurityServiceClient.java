package com.ecom.gateway.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient(value = "security-service", url = "http://localhost:8080")
public interface SecurityServiceClient {

    @RequestMapping(method = RequestMethod.POST, value = "/auth/session/jwt")
    TokenResponse getJwt(@RequestParam(value = "userSessionId") String userSessionId);

    record TokenResponse(String token) {
    }
}


