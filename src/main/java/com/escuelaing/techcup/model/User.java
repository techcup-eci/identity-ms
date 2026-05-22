package com.escuelaing.techcup.model;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * Estado del usuario en el sistema.
     * ACTIVE: puede autenticarse y operar con normalidad.
     * INACTIVE: no puede iniciar sesión. El ADMIN lo cambia,
     * con la restricción de que no puede inactivar a un jugador
     * vinculado a un equipo en torneo ACTIVE o IN_PROGRESS.
     *
     * Reemplaza el campo booleano 'active' anterior.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.INVITED;

    @Column
    private Long usersMsUserId;  // ID from users-and-players-ms for cross-service linking

    public User() {}

    // ── Getters & Setters ────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    /**
     * Convenience method — kept for backward compatibility with code
     * that still checks isActive().
     */
    public Boolean getActive() { return this.status == UserStatus.ACTIVE; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Long getUsersMsUserId() { return usersMsUserId; }
    public void setUsersMsUserId(Long usersMsUserId) { this.usersMsUserId = usersMsUserId; }
}
