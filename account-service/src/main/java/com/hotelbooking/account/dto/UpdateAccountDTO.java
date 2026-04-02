package com.hotelbooking.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateAccountDTO {
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    private String imageUrl;
}
