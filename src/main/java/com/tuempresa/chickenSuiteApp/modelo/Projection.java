package com.tuempresa.chickenSuiteApp.modelo;

import javax.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import lombok.*;

/**
 * Proyección económica para un lote, usada para estimar ingresos y márgenes.
 */
@Entity @Getter @Setter
public class Projection {

    @Id
    @Hidden
    @GeneratedValue(generator="system-uuid")
    @GenericGenerator(name="system-uuid", strategy="uuid")
    @Column(length=32)
    // Identificador único de la proyección
    String oid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList(descriptionProperties = "codigo, especie.nombre, raza.nombre")
    // Lote asociado a la proyección
    FarmBatch lote;

    @Required
    @Money
    // Precio estimado de venta por kilogramo
    BigDecimal precioEsperadoPorKilogramo;

    @Required
    @Money
    BigDecimal costosEstimados;

    // Peso vivo total estimado (dato de entrada)
    @Money
    BigDecimal pesoVivoEstimadoKilogramos;

    // ===== PROPIEDADES CALCULADAS =====

    /**
     * Ingreso proyectado = peso vivo estimado * precio por kg.
     */
    @ReadOnly
    @Depends("pesoVivoEstimadoKilogramos, precioEsperadoPorKilogramo")
    @Money
    public BigDecimal getIngresoProyectado() {
        if (pesoVivoEstimadoKilogramos == null || precioEsperadoPorKilogramo == null) {
            return BigDecimal.ZERO;
        }
        return pesoVivoEstimadoKilogramos.multiply(precioEsperadoPorKilogramo);
    }

    /**
     * Margen proyectado = ingreso proyectado - costos.
     */
    @ReadOnly
    @Depends("pesoVivoEstimadoKilogramos, precioEsperadoPorKilogramo, costosEstimados")
    @Money
    public BigDecimal getMargenProyectado() {
        BigDecimal ingreso = getIngresoProyectado();
        if (costosEstimados == null) return ingreso;
        return ingreso.subtract(costosEstimados);
    }
}
