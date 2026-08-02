package com.coinmarket.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "categories", schema = "coin_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// parent/children are lazy associations and are never exposed via the API;
// ignoring them keeps Redis cache serialization safe (no lazy-loading / recursion).
@JsonIgnoreProperties({"parent", "children"})
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;

    @OneToMany(mappedBy = "parent")
    @OrderBy("sortOrder ASC")
    private List<Category> children;
}
