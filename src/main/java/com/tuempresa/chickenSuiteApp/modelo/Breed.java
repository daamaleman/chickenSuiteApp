package com.tuempresa.chickenSuiteApp.modelo;

import javax.persistence.*;

import com.tuempresa.chickenSuiteApp.modelo.Purpose;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

@Entity @Getter @Setter
public class Breed {

    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(length=32)
    String oid;

    @Column(length=50)
    @Required
    String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList // se muestra como combo
    Species species;

    @Enumerated(EnumType.STRING)
    @Required
    Purpose purpose;   // MEAT, EGG, DUAL

    int growthDaysStd; // días estándar de crecimiento
}