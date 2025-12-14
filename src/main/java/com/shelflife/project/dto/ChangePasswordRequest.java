package com.shelflife.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Old password cannot be empty")
    @NotNull(message = "Old password cannot be empty")
    private String oldPassword;

    @NotBlank(message = "New password cannot be empty")
    @NotNull(message = "New password cannot be empty")
    @Size(min = 6, message = "The new password should be at least 6 characters")
    private String newPassword;

    @NotBlank(message = "Password repeat cannot be empty")
    @NotNull(message = "Password repeat cannot be empty")
    private String newPasswordRepeat;
}
