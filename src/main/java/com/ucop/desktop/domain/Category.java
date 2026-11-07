package com.ucop.desktop.domain;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CATEGORY")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ParentID")
    private Category parent;
    @Column(nullable = false, length = 100)
    private String categoryName;
    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();
    public Integer getCategoryId() { return categoryId; }
    public Category getParent() { return parent; }
    public void setParent(Category parent) { this.parent = parent; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    @Override public String toString() { return categoryName; }
}
