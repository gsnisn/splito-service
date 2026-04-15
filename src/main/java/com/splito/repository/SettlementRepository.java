package com.splito.repository;

import com.splito.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroupId(Long groupId);
    boolean existsByFromUserIdOrToUserId(Long fromUserId, Long toUserId);
}
