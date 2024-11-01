package com.ecom.gateway.clients;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.Serializable;

@FeignClient(value = "security-service", url = "http://localhost:8080")
public interface SecurityServiceClient {

    @RequestMapping(method = RequestMethod.POST, value = "/auth/session/verify")
    LoginStatusResponse verifyAndGetSession();

    @RequestMapping(method = RequestMethod.POST, value = "/auth/session/jwt")
    TokenResponse getJwt(@RequestParam(value = "userSessionId") String userSessionId);

    record TokenResponse(String token) {
    }

    record LoginStatusResponse(Boolean loggedIn, String sessionId)
            implements Serializable {
    }
}


