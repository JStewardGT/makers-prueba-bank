package com.makers.loansystem.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataLoanRepository extends JpaRepository<JpaLoanEntity, Long> {
    List<JpaLoanEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<JpaLoanEntity> findAllByOrderByCreatedAtDesc();
}
