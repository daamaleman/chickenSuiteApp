package com.tuempresa.chickenSuiteApp.modelo;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

import java.util.Collection;
import javax.persistence.OneToMany;
import org.openxava.annotations.ListProperties;

import com.tuempresa.chickenSuiteApp.enums.Stage;

/**
 * Lote de producción: conjunto de aves gestionadas como unidad.
 */
@Entity @Getter @Setter
public class FarmBatch {

    // Versión: colecciones y propiedades en español y más descriptivas

    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(length=32)
    // Identificador único del registro
    String oid;

    @Column(length=20)
    @Required
    // Código identificador del lote
    String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList
    Species especie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList
    Breed raza;

    // Eventos diarios del lote
    @OneToMany(mappedBy = "lote")
    @ListProperties("fecha, tipo, kilogramosAlimento, muertos, costo")
    Collection<DailyEvent> eventos;

    // Cantidad inicial de aves en el lote
    int cantidadInicial;
    // Cantidad actual de aves vivas
    int cantidadVivaActual;
    // Peso objetivo por ave en gramos
    int pesoObjetivoGramos;

    // Fechas del ciclo del lote
    LocalDate fechaInicio;
    LocalDate fechaFinPlaneada;

    @Enumerated(EnumType.STRING)
    Stage etapa;              // Etapas: INCUBACION, CRIA, CRECIMIENTO, ENGORDE, VENDIDO

    @TextArea
    String notas;


    @ReadOnly
    @Depends("cantidadInicial, cantidadVivaActual")
    public BigDecimal getTasaMortalidadPorcentaje() {
        if (cantidadInicial <= 0) return BigDecimal.ZERO;
        int muertos = cantidadInicial - cantidadVivaActual;
        BigDecimal muertosBd = new BigDecimal(muertos);
        BigDecimal initBd = new BigDecimal(cantidadInicial);
        return muertosBd
                .multiply(new BigDecimal("100"))
                .divide(initBd, 2, RoundingMode.HALF_UP); // 2 decimales
    }

    // Porcentaje de aves vivas respecto a las iniciales.
    @ReadOnly
    @Depends("cantidadInicial, cantidadVivaActual")
    public BigDecimal getTasaVivosPorcentaje() {
        if (cantidadInicial <= 0) return BigDecimal.ZERO;
        BigDecimal vivosBd = new BigDecimal(cantidadVivaActual);
        BigDecimal initBd = new BigDecimal(cantidadInicial);
        return vivosBd
                .multiply(new BigDecimal("100"))
                .divide(initBd, 2, RoundingMode.HALF_UP);
    }
}
