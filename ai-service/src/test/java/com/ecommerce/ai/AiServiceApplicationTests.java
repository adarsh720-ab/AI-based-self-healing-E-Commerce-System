package com.ecommerce.ai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:postgresql://localhost:5432/ai_db",
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.kafka.bootstrap-servers=localhost:9092",
        "spring.ai.ollama.base-url=http://localhost:11434"
})
class AiServiceApplicationTests {

    @Test
    void contextLoads() {
    }
}