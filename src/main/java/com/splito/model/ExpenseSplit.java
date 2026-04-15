package com.splito.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expense_split")
@SQLDelete(sql = "UPDATE expense_split SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class ExpenseSplit extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private Expense expense;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private SplitoUser user;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;
}
