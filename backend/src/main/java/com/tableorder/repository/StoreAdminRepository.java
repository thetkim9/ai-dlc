package com.tableorder.repository;

import com.tableorder.entity.Store;
import com.tableorder.entity.StoreAdmin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreAdminRepository extends JpaRepository<StoreAdmin, Long> {
    Optional<StoreAdmin> findByStoreAndUsername(Store store, String username);
}
