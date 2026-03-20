package com.tableorder.repository;

import com.tableorder.entity.Category;
import com.tableorder.entity.Menu;
import com.tableorder.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByStoreAndCategoryOrderByDisplayOrder(Store store, Category category);
    Optional<Menu> findByIdAndStore(Long id, Store store);
    List<Menu> findAllByIdInAndStore(List<Long> ids, Store store);
    int countByCategory(Category category);
}
