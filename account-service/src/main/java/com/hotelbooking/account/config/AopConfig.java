package com.hotelbooking.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
public class AopConfig {
    // This configuration enables AOP for @RequirePermission annotation
}
