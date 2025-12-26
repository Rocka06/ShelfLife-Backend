package com.shelflife.project.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateProductRequest {
    @NotBlank(message = "Name cannot be empty")
    @NotNull(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Category cannot be empty")
    @NotNull(message = "Category cannot be empty")
    private String category;

    @Length(max = 40, message = "The barcode can only be 40 characters long")
    private String barcode;

    @NotBlank(message = "Expiration delta cannot be empty")
    @NotNull(message = "Expiration delta cannot be empty")
    @Min(value = 1, message = "Expiration delta must be larger than 0")
    private int expirationDaysDelta;

    @NotBlank(message = "Running low cannot be empty")
    @NotNull(message = "Running low cannot be empty")
    @Min(value = 1, message = "Running low must be larger than 0")
    private int runningLow;
}
