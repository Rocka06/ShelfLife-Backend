package com.shelflife.project.exceptions;

public class BarcodeExistsException extends RuntimeException {
    public BarcodeExistsException(String barcode) {
        super("Barcode already exists: " + barcode);
    }
}
