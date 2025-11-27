package com.tuempresa.chickenSuiteApp.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.Hidden;
import org.openxava.annotations.Required;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Especie animal (por ejemplo: Gallina, Pato).
 * Representa la clasificación biológica usada en el sistema.
 */
@Entity
@Getter @Setter
public class Species {
    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    // Identificador único del registro
    String oid;

    @Column(length = 40)
    @Required
    String nombre;

    @Column(length = 100)
    String descripcion;
}
