package com.hotelbooking.account.config;

import com.hotelbooking.account.grpc.JwtAuthenticationInterceptor;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcConfig {

    private final JwtAuthenticationInterceptor jwtAuthenticationInterceptor;

    public GrpcConfig(JwtAuthenticationInterceptor jwtAuthenticationInterceptor) {
        this.jwtAuthenticationInterceptor = jwtAuthenticationInterceptor;
    }

    /**
     * Register JWT interceptor for all gRPC services
     * The grpc-spring-boot-starter will automatically apply this interceptor
     */
    @Bean
    public GrpcServerConfigurer grpcServerConfigurer() {
        return serverBuilder -> serverBuilder.intercept(jwtAuthenticationInterceptor);
    }
}
