package com.splito.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(
        name = "splito_direct_group_pair",
        uniqueConstraints = @UniqueConstraint(name = "uq_direct_pair", columnNames = {"user_low", "user_high"})
)
public class DirectGroupPair extends BaseEntity {

    @Id
    @Column(name = "group_id")
    private Long groupId;

    @Column(name = "user_low", nullable = false)
    private Long userLow;

    @Column(name = "user_high", nullable = false)
    private Long userHigh;
}
