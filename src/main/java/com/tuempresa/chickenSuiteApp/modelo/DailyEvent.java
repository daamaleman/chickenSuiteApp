package com.tuempresa.chickenSuiteApp.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;

import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

import com.tuempresa.chickenSuiteApp.enums.EventType;

/**
 * Evento diario que registra acciones o sucesos asociados a un lote.
 */
@Entity @Getter @Setter
public class DailyEvent {

    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(length=32)
    // Identificador único del evento
    String oid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "codigo, especie.nombre, raza.nombre")
    FarmBatch lote;

    @Required
    LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Required
    EventType tipo;

    @Money
    BigDecimal costo;

    // Solo si tipo == MORTALIDAD
    int muertos;
    // Solo si tipo == ALIMENTACION
    BigDecimal kilogramosAlimento;

    @TextArea
    String notas;
}
