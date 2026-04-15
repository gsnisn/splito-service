package com.splito.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "splito_group")
@SQLDelete(sql = "UPDATE splito_group SET deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class SplitoGroup extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany
    @JoinTable(
            name = "splito_group_members",
            joinColumns = @JoinColumn(name = "splito_group_id"),
            inverseJoinColumns = @JoinColumn(name = "members_id")
    )
    private List<SplitoUser> members;

    private boolean isDirect;
}


