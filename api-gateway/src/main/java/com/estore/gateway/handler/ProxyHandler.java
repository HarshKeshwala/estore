package com.estore.gateway.handler;

import com.estore.gateway.config.GatewayConfig;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class ProxyHandler {

    private final WebClient webClient;
    private final GatewayConfig gatewayConfig;

    public ProxyHandler(WebClient.Builder webClientBuilder, GatewayConfig gatewayConfig) {
        this.webClient = webClientBuilder.build();
        this.gatewayConfig = gatewayConfig;
    }

    public Mono<ServerResponse> proxyToUserService(ServerRequest request) {
        return proxyRequest(request, gatewayConfig.getUserServiceUrl(), "/api");
    }

    public Mono<ServerResponse> proxyToProductService(ServerRequest request) {
        return proxyRequest(request, gatewayConfig.getProductServiceUrl(), "/api");
    }

    public Mono<ServerResponse> proxyToOrderService(ServerRequest request) {
        return proxyRequest(request, gatewayConfig.getOrderServiceUrl(), "/api");
    }

    private Mono<ServerResponse> proxyRequest(ServerRequest request, String baseUrl, String stripPrefix) {
        String path = request.path();
        if (path.startsWith(stripPrefix)) {
            path = path.substring(stripPrefix.length());
        }

        String targetUrl = baseUrl + path;
        HttpMethod method = request.method();

        WebClient.RequestBodySpec requestSpec = webClient.method(method)
                .uri(targetUrl)
                .headers(headers -> {
                    request.headers().asHttpHeaders().forEach((name, values) -> {
                        if (!name.equalsIgnoreCase("Host") && !name.equalsIgnoreCase("Content-Length")
                                && !name.equalsIgnoreCase("Authorization")) {
                            headers.addAll(name, values);
                        }
                    });
                });

        Mono<WebClient.ResponseSpec> responseSpecMono;
        if (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH) {
            responseSpecMono = request.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .map(body -> requestSpec.body(BodyInserters.fromValue(body)).retrieve());
        } else {
            responseSpecMono = Mono.just(requestSpec.retrieve());
        }

        return responseSpecMono.flatMap(responseSpec ->
                responseSpec.toEntity(String.class)
                        .flatMap(response -> {
                            ServerResponse.BodyBuilder bodyBuilder = ServerResponse.status(response.getStatusCode());
                            response.getHeaders().forEach((name, values) -> {
                                if (!name.equalsIgnoreCase("Transfer-Encoding")) {
                                    bodyBuilder.header(name, values.toArray(new String[0]));
                                }
                            });
                            String body = response.getBody();
                            if (body != null && !body.isEmpty()) {
                                return bodyBuilder.bodyValue(body);
                            } else {
                                return bodyBuilder.build();
                            }
                        })
                        .onErrorResume(e -> ServerResponse.status(502).bodyValue("Gateway error: " + e.getMessage()))
        );
    }
}
