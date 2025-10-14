package com.david.gateway.support;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class AuthClient {

    private final WebClient webClient;

    public AuthClient(@LoadBalanced WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://springoj-authentication").build();
    }

    public Mono<IntrospectResponse> introspect(String token) {
        return webClient
                .post()
                .uri("/api/auth/introspect")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new IntrospectRequest(token))
                .retrieve()
                .bodyToMono(IntrospectResponse.class);
    }
}
