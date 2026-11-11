package com.makers.loansystem.infrastructure.adapter.out.persistence;

import com.makers.loansystem.domain.model.Loan;
import com.makers.loansystem.domain.port.LoanRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LoanJpaAdapter implements LoanRepositoryPort {

    private final SpringDataLoanRepository springDataLoanRepository;

    public LoanJpaAdapter(SpringDataLoanRepository springDataLoanRepository) {
        this.springDataLoanRepository = springDataLoanRepository;
    }

    @Override
    public Loan save(Loan loan) {
        JpaLoanEntity entity = JpaLoanEntity.fromDomain(loan);
        JpaLoanEntity saved = springDataLoanRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Loan> findById(Long id) {
        return springDataLoanRepository.findById(id).map(JpaLoanEntity::toDomain);
    }

    @Override
    public List<Loan> findByUserId(Long userId) {
        return springDataLoanRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(JpaLoanEntity::toDomain)
                .toList();
    }

    @Override
    public List<Loan> findAll() {
        return springDataLoanRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(JpaLoanEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataLoanRepository.deleteById(id);
    }
}
