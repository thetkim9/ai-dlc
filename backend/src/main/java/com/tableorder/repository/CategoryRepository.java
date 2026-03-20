package com.tableorder.repository;

import com.tableorder.entity.Category;
import com.tableorder.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findAllByStoreOrderByDisplayOrder(Store store);
}
