package com.example.adela.config;

import com.example.adela.clients.GroupClient;
import com.example.adela.clients.UsuarioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import reactor.core.publisher.Mono;

@Configuration
public class ClientConfig {

    @Value("${api-gateway.url:http://localhost:8060}")
    private String apiGatewayUrl;

    // WebClient para UsuarioClient (ms-auth)
    @Bean("authWebClient")
    public WebClient authWebClient() {
        return WebClient.builder()
            .baseUrl(apiGatewayUrl) // Usamos API Gateway para ambos
            .defaultHeader("Content-Type", "application/json")
            .filter(jwtTokenPropagationFilter())
            .filter(loggingFilter("ms-auth"))
            .build();
    }

    // WebClient para GroupClient (ms-grupos) - ¡ESTE TE FALTABA!
    @Bean("groupsWebClient")
    public WebClient groupsWebClient() {
        return WebClient.builder()
            .baseUrl(apiGatewayUrl) // Mismo API Gateway
            .defaultHeader("Content-Type", "application/json")
            .filter(jwtTokenPropagationFilter())
            .filter(loggingFilter("ms-grupos"))
            .build();
    }

    @Bean
    public UsuarioClient usuarioClient() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(authWebClient()))
            .build();

        return factory.createClient(UsuarioClient.class);
    }

    // ¡ESTE BEAN TE FALTABA!
    @Bean
    public GroupClient groupClient() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(groupsWebClient()))
            .build();

        return factory.createClient(GroupClient.class);
    }

    /**
     * Filtro que propaga el token JWT de la petición actual
     */
    private ExchangeFilterFunction jwtTokenPropagationFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (attributes != null) {
                String authHeader = attributes.getRequest().getHeader("Authorization");
                
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    ClientRequest modifiedRequest = ClientRequest.from(clientRequest)
                        .header("Authorization", authHeader)
                        .build();
                    
                    System.out.println("🔐 Token JWT propagado");
                    return Mono.just(modifiedRequest);
                }
            }
            
            System.out.println("⚠️ No se encontró token JWT para propagar");
            return Mono.just(clientRequest);
        });
    }

    /**
     * Filtro para logging de requests con identificador de servicio
     */
    private ExchangeFilterFunction loggingFilter(String serviceName) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("📡 [" + serviceName + "] Llamando a: " + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }
}
