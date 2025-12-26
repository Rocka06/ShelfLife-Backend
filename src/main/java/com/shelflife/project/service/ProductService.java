package com.shelflife.project.service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.shelflife.project.exception.BarcodeExistsException;
import com.shelflife.project.exception.ItemNotFoundException;
import com.shelflife.project.model.Product;
import com.shelflife.project.model.User;
import com.shelflife.project.repository.ProductRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<String> getCategories() {
        return productRepository.getCategories();
    }

    public Product getProductByID(final long id) throws ItemNotFoundException {
        Optional<Product> product = productRepository.findById(id);

        if (!product.isPresent())
            throw new ItemNotFoundException();

        return product.get();
    }

    public Product getProductByBarcode(final String barcode) throws ItemNotFoundException {
        Optional<Product> product = productRepository.findByBarcode(barcode);

        if (!product.isPresent())
            throw new ItemNotFoundException();

        return product.get();
    }

    public boolean productExistsByID(final long id) {
        return productRepository.existsById(id);
    }

    public boolean productExistsByBarcode(final String barcode) {
        return productRepository.findByBarcode(barcode).isPresent();
    }

    @Transactional
    public Product saveProduct(Product product) throws BarcodeExistsException, InvalidParameterException {
        if (!product.getBarcode().isBlank())
            if (productExistsByBarcode(product.getBarcode()))
                throw new BarcodeExistsException(product.getBarcode());

        if (product.getName().isBlank())
            throw new InvalidParameterException("name");

        if (product.getCategory().isBlank())
            throw new InvalidParameterException("category");

        if (product.getExpirationDaysDelta() <= 0) {
            throw new InvalidParameterException("expirationDaysDelta");
        }

        if (product.getRunningLow() <= 0) {
            throw new InvalidParameterException("runningLow");
        }

        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(long productId, Product product, User currentUser)
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

        if (product.getExpirationDaysDelta() > 0) {
            productDB.setExpirationDaysDelta(product.getExpirationDaysDelta());
        } else {
            throw new InvalidParameterException("expirationDaysDelta");
        }

        if (product.getRunningLow() > 0) {
            productDB.setRunningLow(product.getRunningLow());
        } else {
            throw new InvalidParameterException("runningLow");
        }

        return productRepository.save(productDB);
    }

    @Transactional
    public void removeProduct(long id, User currentUser) throws ItemNotFoundException {
        Product product = getProductByID(id);

        if (product.getOwnerId() != currentUser.getId() && !currentUser.isAdmin())
            throw new AccessDeniedException("");

        productRepository.deleteById(id);
    }
}
