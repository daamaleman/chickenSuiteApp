package com.tuempresa.chickenSuiteApp.modelo;

import javax.persistence.*;

import com.tuempresa.chickenSuiteApp.enums.Purpose;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;
import javax.validation.constraints.*;

/**
 * Raza: agrupa características productivas dentro de una especie.
 */
@Entity @Getter @Setter
public class Breed {

    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(length=32)
    // Identificador único de la raza
    String oid;

    @Column(length=50, unique=true)
    @Required
    @NotBlank(message = "El nombre de la raza es obligatorio")
    String nombre; // Nombre de la raza

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList // se muestra como combo
    Species especie;

    @Enumerated(EnumType.STRING)
    @Required
    Purpose proposito;   // CARNE, HUEVO, DOBLE_PROPOSITO

    // Días estándar estimados para crecimiento hasta sacrificio
    @Min(value = 1, message = "Los días estándar de crecimiento deben ser al menos 1")
    int diasCrecimientoEstandar;

    /**
     * Devuelve el número de días estándar estimado hasta la cosecha/sacrificio.
     * Útil para proyecciones y planificación desde la interfaz.
     */
    public int expectedHarvestDay() {
        return diasCrecimientoEstandar;
    }
}