package com.learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private long id;

    @Column(name = "role_name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "is_default", nullable = false)
    private boolean defaultRole;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_functions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "function_code"))
    private Set<Permission> functions = new HashSet<>();

    public Role() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefaultRole() {
        return defaultRole;
    }

    public Set<Permission> getFunctions() {
        return functions;
    }

    public void setFunctions(Set<Permission> functions) {
        this.functions = new HashSet<>(functions);
    }
}
