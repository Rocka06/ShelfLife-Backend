package com.shelflife.project.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.shelflife.project.dto.CreateProductRequest;
import com.shelflife.project.dto.UpdateProductRequest;
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

        product.setOwnerId(currentUser.get().getId());

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
    public Product updateProduct(long productId, UpdateProductRequest request, Authentication auth)
            throws BarcodeExistsException, AccessDeniedException {
        Optional<User> currentUser = userService.getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        Product productDB = getProductByID(productId);
        if (currentUser.get().getId() != productDB.getOwnerId() && !currentUser.get().isAdmin())
            throw new AccessDeniedException(null);

        if (request.getName() != null && !request.getName().isBlank())
            productDB.setName(request.getName());

        if (request.getCategory() != null && !request.getCategory().isBlank())
            productDB.setCategory(request.getCategory());

        if (request.getBarcode() != null && !request.getBarcode().isBlank()) {
            if (existsByBarcode(request.getBarcode()))
                throw new BarcodeExistsException(request.getBarcode());

            productDB.setBarcode(request.getBarcode());
        }

        if (request.getExpirationDaysDelta() != null) {
            if (request.getExpirationDaysDelta() < 1)
                throw new IllegalArgumentException("expirationDaysDelta");

            productDB.setExpirationDaysDelta(request.getExpirationDaysDelta());
        }

        if (request.getRunningLow() != null) {
            if (request.getRunningLow() < 1)
                throw new IllegalArgumentException("runningLow");

            productDB.setRunningLow(request.getRunningLow());
        }

        return productRepository.save(productDB);
    }

    @Transactional
    public void removeProduct(long id, Authentication auth) throws AccessDeniedException, ItemNotFoundException {
        Optional<User> currentUser = userService.getUserByAuth(auth);

        if (!currentUser.isPresent())
            throw new AccessDeniedException(null);

        Product product = getProductByID(id);
        if (!currentUser.get().isAdmin() && currentUser.get().getId() != product.getOwnerId())
            throw new AccessDeniedException(null);

        productRepository.deleteById(id);
    }
}
