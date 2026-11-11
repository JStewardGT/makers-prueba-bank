package com.makers.loansystem.infrastructure.adapter.out.persistence;

import com.makers.loansystem.domain.model.User;
import com.makers.loansystem.domain.port.UserRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class UserJpaAdapter implements UserRepositoryPort {

    private final SpringDataUserRepository springDataUserRepository;

    public UserJpaAdapter(SpringDataUserRepository springDataUserRepository) {
        this.springDataUserRepository = springDataUserRepository;
    }

    @Override
    public User save(User user) {
        JpaUserEntity entity = JpaUserEntity.fromDomain(user);
        JpaUserEntity saved = springDataUserRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<User> findById(Long id) {
        return springDataUserRepository.findById(id).map(JpaUserEntity::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmailIgnoreCase(email).map(JpaUserEntity::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return springDataUserRepository.existsByEmailIgnoreCase(email);
    }

    @Override
    public List<User> findAll() {
        return springDataUserRepository.findAll().stream()
                .map(JpaUserEntity::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        springDataUserRepository.deleteById(id);
    }
}
