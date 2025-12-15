package com.shelflife.project.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Setter
@Getter
public class ChangeUserDataRequest {
    @Email(message = "Invalid email")
    private String email;

    @Size(max = 40, message = "The username can only be 40 characters")
    private String username;

    private Boolean isAdmin;
}
