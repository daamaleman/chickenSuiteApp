package com.tuempresa.chickenSuiteApp.modelo;

import com.tuempresa.chickenSuiteApp.enums.Purpose;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * Raza dentro de una especie avícola.
 *
 * Define el propósito productivo (carne, huevo o doble propósito)
 * y los días estándar de crecimiento hasta la cosecha.
 */
@Entity
@Table(name = "CS_BREED")
@Getter
@Setter
@View(members =
        "nombre, especie; " +
                "proposito; " +
                "diasCrecimientoEstandar"
)
@Tab(
        name = "Razas",
        properties = "nombre, especie.nombre, proposito, diasCrecimientoEstandar",
        defaultOrder = "nombre asc"
)
public class Breed {

    // =========================================================
    // Identificador
    // =========================================================

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    private String oid;

    // =========================================================
    // Datos básicos
    // =========================================================

    /**
     * Nombre de la raza (único dentro del sistema).
     */
    @Column(length = 50, unique = true)
    @Required
    @NotBlank(message = "El nombre de la raza es obligatorio")
    private String nombre;

    /**
     * Especie a la que pertenece la raza (por ejemplo: pollo, codorniz).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "nombre")
    private Species especie;

    /**
     * Propósito productivo principal de la raza.
     */
    @Enumerated(EnumType.STRING)
    @Required
    private Purpose proposito;   // CARNE, HUEVO, DOBLE_PROPOSITO

    /**
     * Días estándar estimados de crecimiento hasta la cosecha.
     */
    @Min(value = 1, message = "Los días estándar de crecimiento deben ser al menos 1")
    private int diasCrecimientoEstandar;

    // =========================================================
    // Lógica de dominio
    // =========================================================

    /**
     * Devuelve los días estándar estimados hasta la cosecha.
     * Se usa en proyecciones y planificación del lote.
     */
    public int obtenerDiasHastaCosechaEstandar() {
        return diasCrecimientoEstandar;
    }
}
