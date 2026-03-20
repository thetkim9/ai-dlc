package com.tableorder.repository;

import com.tableorder.entity.SessionStatus;
import com.tableorder.entity.TableEntity;
import com.tableorder.entity.TableSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TableSessionRepository extends JpaRepository<TableSession, Long> {
    Optional<TableSession> findByTableAndStatus(TableEntity table, SessionStatus status);
}
