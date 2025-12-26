package com.shelflife.project.exception;

public class BarcodeExistsException extends RuntimeException {
    public BarcodeExistsException(String barcode) {
        super("Barcode already exists: " + barcode);
    }
}
