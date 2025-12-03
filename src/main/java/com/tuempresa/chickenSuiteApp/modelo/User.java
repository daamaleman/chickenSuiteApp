package com.tuempresa.chickenSuiteApp.modelo;

import com.tuempresa.chickenSuiteApp.enums.Role;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Usuario del sistema Chicken Suite.
 *
 * Representa a una persona que utiliza el sistema, con:
 * - nombre de usuario para iniciar sesión
 * - contraseña
 * - nombre completo
 * - rol funcional
 * - estado activo/inactivo
 */
@Entity
@Table(
        name = "CS_USER",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_CS_USER_USERNAME",
                        columnNames = {"username"}
                )
        }
)
@Getter
@Setter
@View(members =
        "nombreUsuario; " +
                "passwordHash; " +
                "nombreCompleto; " +
                "rol; " +
                "activo"
)
@Tab(
        name = "Usuarios",
        properties = "nombreUsuario, nombreCompleto, rol, activo",
        defaultOrder = "nombreUsuario asc"
)
public class User {

    // =========================================================
    // Identificador (oculto en la interfaz)
    // =========================================================

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    private String oid;

    // =========================================================
    // Datos de identificación
    // =========================================================

    /**
     * Nombre de usuario utilizado para iniciar sesión.
     * Se normaliza a minúsculas y sin espacios alrededor.
     */
    @Column(name = "username", length = 30, nullable = false, unique = true)
    @Required
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String nombreUsuario;

    /**
     * Contraseña del usuario.
     * En un entorno real debería almacenarse como hash seguro.
     */
    @Column(name = "password_hash", length = 120, nullable = false)
    @Required
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Password
    private String passwordHash;

    /**
     * Nombre completo de la persona usuaria.
     */
    @Column(name = "full_name", length = 80, nullable = false)
    @Required
    @NotBlank(message = "El nombre completo es obligatorio")
    private String nombreCompleto;

    /**
     * Rol funcional del usuario dentro del sistema.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", length = 20, nullable = false)
    @Required
    @NotNull(message = "El rol es obligatorio")
    private Role rol;

    /**
     * Indica si el usuario está activo.
     */
    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    // =========================================================
    // Lógica de dominio (permisos básicos)
    // =========================================================

    /**
     * Indica si la persona usuaria puede editar costos y proyecciones económicas.
     */
    public boolean puedeEditarCostos() {
        return activo && rol == Role.ADMINISTRADOR;
    }

    /**
     * Indica si la persona usuaria puede ver reportes e indicadores.
     */
    public boolean puedeVerReportes() {
        if (!activo) {
            return false;
        }
        return rol == Role.ADMINISTRADOR || rol == Role.ANALISTA;
    }

    // =========================================================
    // Normalización de datos (ciclo de vida JPA)
    // =========================================================

    /**
     * Normaliza algunos campos de texto antes de guardar:
     * - nombreUsuario se guarda en minúsculas y sin espacios alrededor.
     * - nombreCompleto se guarda sin espacios al inicio ni al final.
     */
    @PrePersist
    @PreUpdate
    private void normalizarDatos() {
        if (nombreUsuario != null) {
            nombreUsuario = nombreUsuario.trim().toLowerCase();
        }
        if (nombreCompleto != null) {
            nombreCompleto = nombreCompleto.trim();
        }
    }
}
