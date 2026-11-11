package com.makers.loansystem.domain.model;

import java.time.Instant;
import java.util.Objects;

public class User {
    private Long id;
    private String email;
    private String password;
    private String fullName;
    private Role role;
    private Instant createdAt;

    public User() {
    }

    public User(Long id, String email, String password, String fullName, Role role, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.role = role != null ? role : Role.ROLE_USER;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public static User create(String email, String encodedPassword, String fullName, Role role) {
        return new User(null, email, encodedPassword, fullName, role, Instant.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) || (email != null && email.equalsIgnoreCase(user.email));
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }
}
