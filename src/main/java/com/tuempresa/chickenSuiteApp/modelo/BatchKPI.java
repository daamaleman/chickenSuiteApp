package com.tuempresa.chickenSuiteApp.modelo;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Indicadores clave de desempeño (KPI) asociados a un lote específico.
 *
 * Los valores pueden venir de análisis externos o de registros históricos.
 * Esta entidad permite guardar y visualizar:
 * - gananciaDiariaPromedioGramos
 * - indiceConversionAlimenticia (FCR)
 * - tasaMortalidadPorcentaje
 * - costoProyectado
 * - ingresoProyectado
 * - margenNeto (calculado)
 */
@Entity
@Getter
@Setter
@View(members =
        "lote;" +
                "gananciaDiariaPromedioGramos, indiceConversionAlimenticia, tasaMortalidadPorcentaje;" +
                "costoProyectado, ingresoProyectado, margenNeto"
)
@Tab(properties =
        "lote.codigo, gananciaDiariaPromedioGramos, indiceConversionAlimenticia, " +
                "tasaMortalidadPorcentaje, costoProyectado, ingresoProyectado, margenNeto"
)
public class BatchKPI {

    // =========================================================
    // Identificador
    // =========================================================

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    // Identificador único del registro de KPI
    private String oid;

    // =========================================================
    // Relación con el lote
    // =========================================================

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull(message = "Debe seleccionar el lote al que corresponden los indicadores")
    @DescriptionsList(descriptionProperties = "codigo, especie.nombre, raza.nombre")
    // Lote al que pertenecen estos indicadores
    private FarmBatch lote;

    // =========================================================
    // Indicadores técnicos
    // =========================================================

    /**
     * Ganancia diaria promedio por ave, en gramos.
     * Por ejemplo: 60.00 gramos/día.
     */
    @Money
    private BigDecimal gananciaDiariaPromedioGramos;

    /**
     * Índice de conversión alimenticia (Feed Conversion Ratio, FCR).
     * Fórmula típica: kg de alimento / kg de ganancia de peso.
     * Ejemplo: 1.80
     */
    @Money
    private BigDecimal indiceConversionAlimenticia;

    /**
     * Tasa de mortalidad expresada como porcentaje.
     * Ejemplo: 5.50 representa 5.50 %
     */
    @Money
    private BigDecimal tasaMortalidadPorcentaje;

    // =========================================================
    // Proyección económica
    // =========================================================

    @Money
    @DecimalMin(value = "0.0", inclusive = true,
            message = "El costo proyectado no puede ser negativo")
    // Costo total proyectado para el lote
    private BigDecimal costoProyectado;

    @Money
    @DecimalMin(value = "0.0", inclusive = true,
            message = "El ingreso proyectado no puede ser negativo")
    // Ingreso total proyectado para el lote
    private BigDecimal ingresoProyectado;

    // =========================================================
    // Propiedad calculada
    // =========================================================

    /**
     * Margen neto proyectado para el lote.
     *
     * Fórmula:
     * margenNeto = ingresoProyectado - costoProyectado
     */
    @ReadOnly
    @Depends("ingresoProyectado, costoProyectado")
    @Money
    public BigDecimal getMargenNeto() {
        if (ingresoProyectado == null && costoProyectado == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal ingreso = ingresoProyectado != null
                ? ingresoProyectado
                : BigDecimal.ZERO;

        BigDecimal costo = costoProyectado != null
                ? costoProyectado
                : BigDecimal.ZERO;

        return ingreso.subtract(costo).setScale(2, RoundingMode.HALF_UP);
    }
}
