package com.escuelaing.techcup.dto;

public class UserServiceResponse {
    private Long id;
    private String email;
    private String nombre;
    private String rol;

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getNombre() { return nombre; }

    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getRol() { return rol; }

    public void setRol(String rol) { this.rol = rol; }

}