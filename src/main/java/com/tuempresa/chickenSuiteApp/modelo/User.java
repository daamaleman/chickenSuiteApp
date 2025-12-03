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
 * Esta entidad representa a los usuarios que acceden al sistema,
 * diferenciando su rol y estado (activo/inactivo).
 */
@Entity
@Getter
@Setter
@View(members =
        "nombreUsuario, passwordHash, nombreCompleto, rol, activo"
)
@Tab(properties =
        "nombreUsuario, nombreCompleto, rol, activo"
)
public class User {

    // =========================================================
    // Identificador
    // =========================================================

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    // Identificador interno del usuario
    private String oid;

    // =========================================================
    // Datos de identificación
    // =========================================================

    @Column(length = 30, unique = true)
    @Required
    @NotBlank(message = "El nombre de usuario es obligatorio")
    // Nombre de usuario utilizado para autenticación
    private String nombreUsuario;

    @Column(length = 120)
    @Required
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    @Password   // Campo tipo password en la interfaz de OpenXava
    // Contraseña (o hash de contraseña) del usuario
    private String passwordHash;

    @Column(length = 80)
    @Required
    @NotBlank(message = "El nombre completo es obligatorio")
    // Nombre completo visible en la interfaz
    private String nombreCompleto;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Required
    @NotNull(message = "El rol es obligatorio")
    // Rol funcional del usuario dentro del sistema
    private Role rol;

    // Indica si el usuario está activo en el sistema
    private boolean activo;

    // =========================================================
    // Métodos de ayuda para permisos básicos
    // =========================================================

    /**
     * Indica si el usuario puede editar costos y proyecciones económicas.
     */
    public boolean puedeEditarCostos() {
        return activo && rol == Role.ADMINISTRADOR;
    }

    /**
     * Indica si el usuario puede ver reportes e indicadores.
     */
    public boolean puedeVerReportes() {
        if (!activo) {
            return false;
        }
        return rol == Role.ADMINISTRADOR || rol == Role.ANALISTA;
    }
}
