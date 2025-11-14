package com.tuempresa.chickenSuiteApp.modelo;

import javax.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

@Entity @Getter @Setter
public class FarmBatch {

    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(length=32)
    String oid;

    @Column(length=20)
    @Required
    String code;              // Código del lote

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList
    Species species;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList
    Breed breed;

    int initialQuantity;
    int currentAlive;
    int targetWeightGr;

    LocalDate startDate;
    LocalDate plannedEndDate;

    @Enumerated(EnumType.STRING)
    Stage stage;              // INCUBATION, BROODING, etc.

    @TextArea
    String notes;
}

