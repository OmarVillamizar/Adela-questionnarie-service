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

    @Bean("authWebClient")
    public WebClient authWebClient() {
        return WebClient.builder()
            .baseUrl(apiGatewayUrl)
            .defaultHeader("Content-Type", "application/json")
            .filter(jwtTokenPropagationFilter())
            .filter(loggingFilter("ms-auth"))
            .build();
    }

    @Bean("groupsWebClient")
    public WebClient groupsWebClient() {
        return WebClient.builder()
            .baseUrl(apiGatewayUrl)
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

    @Bean
    public GroupClient groupClient() {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(WebClientAdapter.create(groupsWebClient()))
            .build();

        return factory.createClient(GroupClient.class);
    }

    /**
     * Filtro que propaga el token JWT de la petición actual
     * MEJORADO: Logging detallado para debugging
     */
    private ExchangeFilterFunction jwtTokenPropagationFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            try {
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    String authHeader = attributes.getRequest().getHeader("Authorization");
                    String requestUri = attributes.getRequest().getRequestURI();

                    System.out.println("🌐 Petición original desde: " + requestUri);
                    System.out.println("   └─ Destino WebClient: " + clientRequest.url());

                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        ClientRequest modifiedRequest = ClientRequest.from(clientRequest)
                            .header("Authorization", authHeader)
                            .build();

                        System.out.println("🔐 ✅ Token JWT propagado exitosamente");
                        return Mono.just(modifiedRequest);
                    } else {
                        System.out.println("⚠️ Authorization header: " + 
                            (authHeader == null ? "NULL" : "No es Bearer (valor: " + authHeader + ")"));
                    }
                } else {
                    System.out.println("❌ RequestAttributes es NULL - No hay contexto HTTP disponible");
                    System.out.println("   └─ URL destino: " + clientRequest.url());
                }
            } catch (IllegalStateException e) {
                System.out.println("❌ IllegalStateException: " + e.getMessage());
                System.out.println("   └─ El contexto del request no está disponible en este thread");
            } catch (Exception e) {
                System.out.println("❌ Error capturando token: " + e.getClass().getSimpleName());
                System.out.println("   └─ Mensaje: " + e.getMessage());
            }

            System.out.println("⚠️ ❌ Petición SIN TOKEN a: " + clientRequest.url());
            return Mono.just(clientRequest);
        });
    }

    /**
     * Filtro para logging de requests con verificación de headers
     */
    private ExchangeFilterFunction loggingFilter(String serviceName) {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            System.out.println("\n📡 [" + serviceName + "] Request:");
            System.out.println("   ├─ URL: " + clientRequest.url());
            System.out.println("   ├─ Method: " + clientRequest.method());
            
            // Verificar si tiene Authorization header
            boolean hasAuth = clientRequest.headers().containsKey("Authorization");
            System.out.println("   └─ Authorization: " + (hasAuth ? "✓ PRESENTE" : "✗ AUSENTE"));
            
            if (!hasAuth) {
                System.out.println("      ⚠️ WARNING: Esta petición NO tiene token!");
            }
            
            return Mono.just(clientRequest);
        });
    }
}