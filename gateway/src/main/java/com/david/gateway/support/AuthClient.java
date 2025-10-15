package com.david.gateway.support;

import com.david.common.http.ApiError;
import com.david.common.http.ApiResponse;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class AuthClient {

    private static final ParameterizedTypeReference<ApiResponse<IntrospectResponse>>
            INTROSPECT_RESPONSE_TYPE =
                    new ParameterizedTypeReference<>() {};

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
                .bodyToMono(INTROSPECT_RESPONSE_TYPE)
                .flatMap(
                        response -> {
                            if (response.isSuccess() && response.data() != null) {
                                return Mono.just(response.data());
                            }
                            ApiError error = response.error();
                            String message =
                                    error != null
                                            ? error.message()
                                            : "Token introspection failed";
                            return Mono.error(new IllegalStateException(message));
                        });
    }
}
