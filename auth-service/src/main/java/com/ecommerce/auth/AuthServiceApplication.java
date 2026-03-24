package com.ecommerce.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(scanBasePackages = {
        "com.ecommerce.auth",
        "com.ecommerce.commons"
})
@EnableFeignClients         // enables UserServiceClient (Feign)
@EnableAspectJAutoProxy     // enables KafkaLogInterceptor from commons
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
