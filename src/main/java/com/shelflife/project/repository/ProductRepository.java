package com.shelflife.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.shelflife.project.model.Product;
import java.util.List;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByName(String name);
    List<Product> findByCategory(String category);
    List<Product> findByOwnerId(long ownerId);
    Optional<Product> findByBarcode(String barcode);

    @Query("select p.category from Product p")
    List<String> getCategories();
}