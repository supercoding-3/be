package com.github.p3.repository;

import com.github.p3.entity.Category;
import com.github.p3.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategory(Category category);
}
