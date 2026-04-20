package com.resumise.backend.repository;

import com.resumise.backend.model.AuthAccount;
import com.resumise.backend.model.AuthProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {
    Optional<AuthAccount> findByProviderAndProviderUserId(AuthProvider provider, String providerUserId);

    Optional<AuthAccount> findFirstByUserIdOrderByIdAsc(Long userId);

    boolean existsByUserIdAndProvider(Long userId, AuthProvider provider);
}
