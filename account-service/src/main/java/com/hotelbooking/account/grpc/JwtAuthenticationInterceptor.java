package com.hotelbooking.account.grpc;

import com.hotelbooking.account.security.CustomUserDetailsService;
import com.hotelbooking.account.service.BlacklistTokenService;
import com.hotelbooking.account.service.JwtService;
import io.grpc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationInterceptor implements ServerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationInterceptor.class);

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final BlacklistTokenService blacklistTokenService;

    // List of methods that don't require authentication
    private static final String[] PUBLIC_METHODS = {
        "/com.hotelbooking.account.AuthService/Login",
        "/com.hotelbooking.account.AuthService/Register",
        "/com.hotelbooking.account.AuthService/RefreshToken",
        "/com.hotelbooking.account.AuthService/VerifyEmail",
        "/com.hotelbooking.account.AuthService/ResendVerificationEmail",
        "/com.hotelbooking.account.AuthService/ForgotPassword",
        "/com.hotelbooking.account.AuthService/ResetPassword",
        "/com.hotelbooking.account.HealthService/Check"
    };

    public JwtAuthenticationInterceptor(JwtService jwtService,
                                        CustomUserDetailsService userDetailsService,
                                        BlacklistTokenService blacklistTokenService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.blacklistTokenService = blacklistTokenService;
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                   Metadata headers,
                                                                   ServerCallHandler<ReqT, RespT> next) {
        String methodName = call.getMethodDescriptor().getFullMethodName();
        
        // Check if the method is public
        if (isPublicMethod(methodName)) {
            return next.startCall(call, headers);
        }

        // Extract authorization header
        String authHeader = headers.get(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            call.close(Status.UNAUTHENTICATED.withDescription("Authorization header missing or invalid"), 
                new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }

        String token = authHeader.substring(7);

        try {
            // Check if token is blacklisted
            if (blacklistTokenService.isTokenBlacklisted(token)) {
                call.close(Status.UNAUTHENTICATED.withDescription("Token has been revoked"), 
                    new Metadata());
                return new ServerCall.Listener<ReqT>() {};
            }

            // Validate token and extract username
            String username = jwtService.extractUsername(token);

            if (username == null) {
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid token"), 
                    new Metadata());
                return new ServerCall.Listener<ReqT>() {};
            }

            // Validate token signature and expiration
            if (!jwtService.isTokenValid(token, username)) {
                call.close(Status.UNAUTHENTICATED.withDescription("Token expired or invalid"), 
                    new Metadata());
                return new ServerCall.Listener<ReqT>() {};
            }

            // Load user details
            var userDetails = userDetailsService.loadUserByUsername(username);

            // Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Proceed with the call
            return next.startCall(call, headers);

        } catch (Exception e) {
            logger.error("Error during JWT authentication", e);
            call.close(Status.UNAUTHENTICATED.withDescription("Authentication failed: " + e.getMessage()), 
                new Metadata());
            return new ServerCall.Listener<ReqT>() {};
        }
    }

    private boolean isPublicMethod(String methodName) {
        for (String publicMethod : PUBLIC_METHODS) {
            if (methodName.equals(publicMethod)) {
                return true;
            }
        }
        return false;
    }
}
