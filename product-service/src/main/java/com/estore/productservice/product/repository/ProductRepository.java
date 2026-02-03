package com.estore.productservice.product.repository;

import com.estore.productservice.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + :quantityChange WHERE p.id = :productId AND p.stockQuantity + :quantityChange >= 0")
    int updateStock(@Param("productId") Long productId, @Param("quantityChange") int quantityChange);
}
