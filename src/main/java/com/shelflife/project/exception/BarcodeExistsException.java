package com.shelflife.project.exception;

import lombok.Getter;

@Getter
public class BarcodeExistsException extends RuntimeException {
    private final String barcode;

    public BarcodeExistsException(String barcode) {
        super("Barcode already exists: " + barcode);
        this.barcode = barcode;
    }
}
