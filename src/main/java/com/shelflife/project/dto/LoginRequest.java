package com.shelflife.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class LoginRequest {
    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be empty")
    private String email;
    private String password;
}
