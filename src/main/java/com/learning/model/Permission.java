package com.learning.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "system_functions")
public class Permission {
    @Id
    @Column(name = "function_code", length = 50)
    private String code;

    public Permission() {
    }

    public Permission(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
