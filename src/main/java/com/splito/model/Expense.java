package com.splito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "expense")
@SQLDelete(sql = "UPDATE expense SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Expense extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne
    @JoinColumn(name = "paid_by_id")
    private SplitoUser paidBy;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private SplitoGroup group;

    @ManyToMany
    @JoinTable(
            name = "expense_split_between",
            joinColumns = @JoinColumn(name = "expense_id"),
            inverseJoinColumns = @JoinColumn(name = "split_between_id")
    )
    private List<SplitoUser> splitBetween;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseSplit> splits = new ArrayList<>();
}

