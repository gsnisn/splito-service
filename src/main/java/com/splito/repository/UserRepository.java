package com.splito.repository;

import com.splito.model.SplitoUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<SplitoUser, Long> {

    Optional<SplitoUser> findByEmail(String email);
    boolean existsByEmail(String email);

}


