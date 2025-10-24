package br.com.stanleydev.backendboilerplate.user.repository;

import br.com.stanleydev.backendboilerplate.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByStripeCustomerId(String stripeCustomerId);

    Optional<User> findByRefreshToken(String refreshToken);

    Optional<User> findByPasswordResetToken(String passwordResetToken);
}