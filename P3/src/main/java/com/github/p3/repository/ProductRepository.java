package com.github.p3.repository;

import com.github.p3.entity.Category;
import com.github.p3.entity.Product;
import com.github.p3.exception.CustomException;
import com.github.p3.exception.ErrorCode;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import org.springframework.data.domain.Pageable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(Category category, Pageable pageable);

    List<Product> findByTitleContainingIgnoreCase(String title);

    List<Product> findByUser_UserId(Integer userId);

    default Product findByIdOrElseThrow(Long id){
        return findById(id)
                .orElseThrow(()-> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
    }
}

