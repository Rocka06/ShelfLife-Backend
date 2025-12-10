package com.shelflife.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class SignUpRequest {

    @Email(message = "Invalid email")
    @NotBlank(message = "Email cannot be empty")
    @NotNull(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Username cannot be empty")
    @Size(max = 40, message = "The username can only be 40 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, message = "The password should be at least 6 characters")
    private String password;

    @NotBlank(message = "Repeat the password")
    private String passwordRepeat;
}
