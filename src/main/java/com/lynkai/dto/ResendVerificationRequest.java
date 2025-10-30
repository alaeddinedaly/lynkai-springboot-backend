package com.lynkai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResendVerificationRequest {
    // Getter and Setter
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

}