package com.tableorder.repository;

import com.tableorder.entity.Order;
import com.tableorder.entity.Store;
import com.tableorder.entity.TableEntity;
import com.tableorder.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findBySessionAndIsHistoryFalseOrderByOrderedAtAsc(TableSession session);
    List<Order> findByTableAndIsHistoryFalseOrderByOrderedAtDesc(TableEntity table);
    Optional<Order> findByIdAndStore(Long id, Store store);

    @Modifying
    @Query("UPDATE Order o SET o.isHistory = :isHistory WHERE o.session.id = :sessionId")
    void updateIsHistoryBySessionId(@Param("sessionId") Long sessionId, @Param("isHistory") Boolean isHistory);
}
