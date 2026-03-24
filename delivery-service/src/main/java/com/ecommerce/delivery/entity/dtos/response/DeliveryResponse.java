package com.ecommerce.delivery.entity.dtos.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryResponse {
    private UUID          id;
    private UUID          orderId;
    private UUID          userId;
    private String        trackingCode;
    private String        status;
    private LocalDateTime estimatedDate;
    private LocalDateTime deliveredAt;
}

