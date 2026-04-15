package com.splito.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Table(name = "settlement")
@SQLDelete(sql = "UPDATE settlement SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Settlement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private SplitoGroup group;

    @ManyToOne(optional = false)
    @JoinColumn(name = "from_user_id")
    private SplitoUser fromUser;

    @ManyToOne(optional = false)
    @JoinColumn(name = "to_user_id")
    private SplitoUser toUser;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
