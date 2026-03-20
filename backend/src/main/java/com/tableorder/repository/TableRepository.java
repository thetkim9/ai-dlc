package com.tableorder.repository;

import com.tableorder.entity.Store;
import com.tableorder.entity.TableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    Optional<TableEntity> findByStoreAndTableNumber(Store store, Integer tableNumber);
    List<TableEntity> findAllByStore(Store store);
}
