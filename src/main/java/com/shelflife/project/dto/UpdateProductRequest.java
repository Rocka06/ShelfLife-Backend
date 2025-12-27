package com.shelflife.project.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class UpdateProductRequest {
    private String name;
    private String category;

    @Length(max = 40, message = "The barcode can only be 40 characters long")
    private String barcode;

    @Min(value = 1, message = "Expiration delta must be larger than 0")
    private Integer expirationDaysDelta;

    @Min(value = 1, message = "Running low must be larger than 0")
    private Integer runningLow;
}
