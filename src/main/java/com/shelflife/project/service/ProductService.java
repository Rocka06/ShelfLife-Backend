package com.shelflife.project.service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.exceptions.BarcodeExistsException;
import com.shelflife.project.exceptions.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    List<String> getCategories() {
        return productRepository.getCategories();
    }

    Product getProductByID(final long id) {
        return productRepository.findById(id).orElse(null);
    }

    Product getProductByBarcode(final String barcode) {
        return productRepository.findByBarcode(barcode).orElse(null);
    }

    boolean productExistsByID(final long id) {
        return productRepository.existsById(id);
    }

    boolean productExistsByBarcode(final String barcode) {
        return productRepository.findByBarcode(barcode).isPresent();
    }

    @Transactional
    Product saveProduct(Product product) throws BarcodeExistsException {
        if (!product.getBarcode().isBlank())
            if (productExistsByBarcode(product.getBarcode()))
                throw new BarcodeExistsException(product.getBarcode());

        return productRepository.save(product);
    }

    @Transactional
    Product updateProduct(long productId, Product product, User currentUser)
            throws InvalidParameterException, AccessDeniedException {
        Product productDB = getProductByID(productId);

        if (!currentUser.isAdmin() && currentUser.getId() != productDB.getOwnerId())
            throw new AccessDeniedException("");

        if (Objects.nonNull(product.getName()) && !product.getName().isBlank()) {
            productDB.setName(product.getName());
        }

        if (Objects.nonNull(product.getCategory()) && !product.getCategory().isBlank()) {
            productDB.setCategory(product.getCategory());
        }

        if (Objects.nonNull(product.getBarcode()) && !product.getBarcode().isBlank()) {
            productDB.setBarcode(product.getBarcode());
        }

        if (product.getExpirationDaysDelta() <= 0) {
            productDB.setExpirationDaysDelta(product.getExpirationDaysDelta());
        } else {
            throw new InvalidParameterException("expirationDaysDelta");
        }

        if (product.getRunningLow() <= 0) {
            productDB.setRunningLow(product.getRunningLow());
        } else {
            throw new InvalidParameterException("runningLow");
        }

        return productRepository.save(productDB);
    }

    @Transactional
    void removeProduct(long id, User currentUser) throws ItemNotFoundException {
        Optional<Product> product = productRepository.findById(id);

        if (!product.isPresent())
            throw new ItemNotFoundException();

        if (product.get().getOwnerId() != currentUser.getId() && !currentUser.isAdmin())
            throw new AccessDeniedException("");

        productRepository.deleteById(id);
    }
}
