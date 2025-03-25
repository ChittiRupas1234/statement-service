package com.statement.services.repository;

import com.statement.services.entity.ArchivedTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArchivedTransactionRepository extends JpaRepository<ArchivedTransaction, UUID> {
}