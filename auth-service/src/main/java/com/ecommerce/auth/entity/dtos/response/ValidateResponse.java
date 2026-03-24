package com.ecommerce.auth.entity.dtos.response;

import lombok.*;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ValidateResponse {
    private boolean valid;
    private UUID userId;
    private String email;
    private String role;
}
