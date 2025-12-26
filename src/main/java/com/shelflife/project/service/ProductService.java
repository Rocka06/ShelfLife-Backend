package com.shelflife.project.service;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.CreateProductRequest;
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

    @Autowired
    private UserService userService;

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

    public boolean existsByBarcode(final String barcode) {
        try {
            getProductByBarcode(barcode);
            return true;
        } catch (ItemNotFoundException e) {
            return false;
        }
    }

    public boolean productExistsByID(final long id) {
        return productRepository.existsById(id);
    }

    public boolean productExistsByBarcode(final String barcode) {
        return productRepository.findByBarcode(barcode).isPresent();
    }

    @Transactional
    public Product saveProduct(CreateProductRequest request, Authentication auth)
            throws AccessDeniedException, BarcodeExistsException, IllegalArgumentException {

        Optional<User> currentUser = userService.getUserByAuth(auth);
        Product product = new Product();

        if (!currentUser.isPresent()) {
            throw new AccessDeniedException(null);
        }

        if (request.getBarcode() != null) {
            if (!request.getBarcode().isBlank()) {
                if (existsByBarcode(request.getBarcode()))
                    throw new BarcodeExistsException(request.getBarcode());

                product.setBarcode(request.getBarcode());
            }
        }

        if (request.getCategory() == null)
            throw new IllegalArgumentException("category");

        if (request.getCategory().isBlank())
            throw new IllegalArgumentException("category");

        product.setCategory(request.getCategory());

        if (request.getName() == null)
            throw new IllegalArgumentException("name");

        if (request.getName().isBlank())
            throw new IllegalArgumentException("name");

        product.setName(request.getName());

        if (request.getExpirationDaysDelta() < 1)
            throw new IllegalArgumentException("expirationDaysDelta");

        if (request.getRunningLow() < 1)
            throw new IllegalArgumentException("runningLow");

        product.setRunningLow(request.getRunningLow());
        product.setExpirationDaysDelta(request.getExpirationDaysDelta());

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
