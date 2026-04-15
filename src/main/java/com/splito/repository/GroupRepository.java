package com.splito.repository;

import com.splito.model.SplitoGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupRepository extends JpaRepository<SplitoGroup, Long> {
    List<SplitoGroup> findByMembersId(Long userId);
}
