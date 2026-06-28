package com.fitflow.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

// Implementa UserDetails para que o Spring Security possa usar este objeto diretamente
// no contexto de autenticação, sem precisar de um adaptador separado.
@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // Serve como "username" para o Spring Security — identificador único de autenticação
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    // Armazenado sempre como hash BCrypt — nunca em texto puro
    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    // UserDetails — o email é o identificador de autenticação
    @Override
    public String getUsername() {
        return email;
    }

    // MVP não usa roles — retornamos lista vazia por ora
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    // Todos os checks de estado da conta retornam true no MVP
    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
