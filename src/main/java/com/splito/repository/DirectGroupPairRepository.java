package com.splito.repository;

import com.splito.model.DirectGroupPair;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DirectGroupPairRepository extends JpaRepository<DirectGroupPair, Long> {
    Optional<DirectGroupPair> findByUserLowAndUserHigh(Long userLow, Long userHigh);
}
