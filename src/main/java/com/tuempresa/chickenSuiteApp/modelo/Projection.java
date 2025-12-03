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
 * Proyección económica asociada a un lote (FarmBatch).
 *
 * El usuario proporciona:
 * - precioEsperadoPorKg
 * - costosEsperados
 *
 * El sistema calcula:
 * - pesoVivoTotalEsperadoKg  = avesVivas * pesoObjetivoGramos / 1000
 * - ingresoEsperado          = pesoVivoTotalEsperadoKg * precioEsperadoPorKg
 * - margenEsperado           = ingresoEsperado - costosEsperados
 */
@Entity
@Table(name = "CS_PROJECTION")
@Getter
@Setter
@View(members =
        "lote;" +
                "precioEsperadoPorKg, costosEsperados;" +
                "pesoVivoTotalEsperadoKg, ingresoEsperado, margenEsperado"
)
@Tab(
        name = "Proyecciones",
        properties = "lote.codigo, precioEsperadoPorKg, costosEsperados, pesoVivoTotalEsperadoKg, ingresoEsperado, margenEsperado",
        defaultOrder = "lote.codigo asc"
)
public class Projection {

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
    // Parámetros de la proyección
    // =========================================================

    /**
     * Lote asociado a la proyección económica.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @Required
    @NotNull(message = "Debe seleccionar un lote para la proyección")
    @DescriptionsList(descriptionProperties = "codigo, especie.nombre, raza.nombre")
    private FarmBatch lote;

    /**
     * Precio esperado por kilogramo de peso vivo.
     * Ingresado por la persona usuaria.
     */
    @NotNull(message = "Debe ingresar el precio esperado por kilogramo")
    @Money
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "El precio esperado por kilogramo debe ser mayor que 0"
    )
    private BigDecimal precioEsperadoPorKg;

    /**
     * Costos totales esperados del lote.
     * Incluye alimento, vacunas, servicios, etc.
     */
    @NotNull(message = "Debe ingresar los costos esperados del lote")
    @Money
    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Los costos esperados no pueden ser negativos"
    )
    private BigDecimal costosEsperados;

    // =========================================================
    // Propiedades calculadas
    // =========================================================

    /**
     * Peso vivo total esperado en kilogramos.
     *
     * Fórmula:
     * pesoVivoTotalEsperadoKg =
     *     lote.cantidadVivaActual * lote.pesoObjetivoGramos / 1000
     */
    @ReadOnly
    @Depends("lote")
    @Money
    public BigDecimal getPesoVivoTotalEsperadoKg() {
        if (lote == null) {
            return BigDecimal.ZERO;
        }

        int cantidadAvesVivas = lote.getCantidadVivaActual();
        int pesoObjetivoPorAveGramos = lote.getPesoObjetivoGramos();

        if (cantidadAvesVivas <= 0 || pesoObjetivoPorAveGramos <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal avesVivas = BigDecimal.valueOf(cantidadAvesVivas);
        BigDecimal pesoObjetivoGramos = BigDecimal.valueOf(pesoObjetivoPorAveGramos);

        BigDecimal pesoTotalGramos = avesVivas.multiply(pesoObjetivoGramos);

        return pesoTotalGramos.divide(
                BigDecimal.valueOf(1000),
                2,
                RoundingMode.HALF_UP
        );
    }

    /**
     * Ingreso esperado para el lote.
     *
     * Fórmula:
     * ingresoEsperado = pesoVivoTotalEsperadoKg * precioEsperadoPorKg
     */
    @ReadOnly
    @Depends("lote, precioEsperadoPorKg")
    @Money
    public BigDecimal getIngresoEsperado() {
        if (precioEsperadoPorKg == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal pesoVivoTotalKg = getPesoVivoTotalEsperadoKg();
        return pesoVivoTotalKg.multiply(precioEsperadoPorKg);
    }

    /**
     * Margen esperado del lote.
     *
     * Fórmula:
     * margenEsperado = ingresoEsperado - costosEsperados
     */
    @ReadOnly
    @Depends("lote, precioEsperadoPorKg, costosEsperados")
    @Money
    public BigDecimal getMargenEsperado() {
        BigDecimal ingreso = getIngresoEsperado();

        if (costosEsperados == null) {
            return ingreso;
        }

        return ingreso.subtract(costosEsperados);
    }
}
