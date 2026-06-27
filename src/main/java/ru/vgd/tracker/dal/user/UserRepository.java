package ru.vgd.tracker.dal.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> loadUserWithRolesByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.email = :email")
    Optional<User> loadUserWithRolesByEmail(@Param("email") String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
