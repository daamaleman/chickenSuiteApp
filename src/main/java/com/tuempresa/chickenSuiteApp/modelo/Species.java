package com.tuempresa.chickenSuiteApp.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

/**
 * Especie avícola (por ejemplo: gallina, pato, codorniz).
 *
 * Es la clasificación biológica base sobre la que se definen
 * las razas que maneja el sistema.
 */
@Entity
@Table(name = "CS_SPECIES")
@Getter
@Setter
@View(members = "nombre; descripcion")
@Tab(
        name = "Especies",
        properties = "nombre, descripcion",
        defaultOrder = "nombre asc"
)
public class Species {

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
     * Nombre de la especie (único dentro del sistema).
     * Ejemplos: Gallina, Pato, Codorniz.
     */
    @Column(length = 40, unique = true)
    @Required
    @NotBlank(message = "El nombre de la especie es obligatorio")
    private String nombre;

    /**
     * Descripción corta o notas sobre la especie.
     */
    @Column(length = 100)
    private String descripcion;
}
