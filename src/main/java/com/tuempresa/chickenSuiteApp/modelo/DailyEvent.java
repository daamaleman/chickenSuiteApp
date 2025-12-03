package com.tuempresa.chickenSuiteApp.modelo;

import com.tuempresa.chickenSuiteApp.enums.EventType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;
import org.openxava.calculators.CurrentLocalDateCalculator;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Evento diario que registra acciones o sucesos asociados a un lote.
 *
 * Ejemplos de uso:
 * - Alimentación (ALIMENTACION)
 * - Vacunación (VACUNACION)
 * - Limpieza (LIMPIEZA)
 * - Mortalidad (MORTALIDAD)
 *
 * La lógica que modifica el estado del lote (por ejemplo, descontar aves vivas)
 * se realiza desde la entidad FarmBatch mediante métodos de dominio como
 * registerEvent y registerMortality.
 */
@Entity
@Getter
@Setter
@View(members =
        "lote;" +
                "fecha, tipo;" +
                "avesPerdidas, kilogramosAlimento, costo;" +
                "notas"
)
@Tab(properties =
        "fecha, tipo, lote.codigo, avesPerdidas, kilogramosAlimento, costo"
)
public class DailyEvent {

    // =========================================================
    // Identificador
    // =========================================================

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    // Identificador único del evento
    private String oid;

    // =========================================================
    // Relación con el lote
    // =========================================================

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "codigo, especie.nombre, raza.nombre")
    // Lote al que pertenece este evento
    private FarmBatch lote;

    // =========================================================
    // Datos principales del evento
    // =========================================================

    @Required
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    // Fecha del evento. Por defecto se toma la fecha actual.
    private LocalDate fecha;

    @Enumerated(EnumType.STRING)
    @Required
    // Tipo de evento: ALIMENTACION, VACUNACION, LIMPIEZA, MORTALIDAD, etc.
    private EventType tipo;

    @Money
    // Costo asociado al evento (alimento, vacuna, servicio, etc.)
    private BigDecimal costo;

    // Solo si tipo == MORTALIDAD: número de aves perdidas en este evento
    @Min(value = 0, message = "Las aves perdidas deben ser mayores o iguales a 0")
    private int avesPerdidas;

    // Solo si tipo == ALIMENTACION: kilogramos de alimento suministrados
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Los kilogramos de alimento deben ser mayores que 0"
    )
    private BigDecimal kilogramosAlimento;

    @TextArea
    // Notas u observaciones del evento
    private String notas;

    // =========================================================
    // Validaciones específicas por tipo de evento
    // =========================================================

    /**
     * Si el evento es de mortalidad, debe indicar cuántas aves murieron.
     */
    @AssertTrue(message = "Si el evento es de mortalidad, debe indicar cuántas aves murieron")
    public boolean isValidAvesPerdidas() {
        if (tipo != EventType.MORTALIDAD) {
            return true;
        }
        return avesPerdidas > 0;
    }

    /**
     * Si el evento es de alimentación, debe indicar los kilogramos de alimento.
     */
    @AssertTrue(message = "Si el evento es de alimentación, debe ingresar los kilogramos de alimento suministrados")
    public boolean isValidKilogramosAlimento() {
        if (tipo != EventType.ALIMENTACION) {
            return true;
        }
        return kilogramosAlimento != null
                && kilogramosAlimento.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Si el evento es de alimentación o vacunación, el costo debe ser mayor o igual que cero.
     * Para otros tipos de evento no se aplica esta restricción.
     */
    @AssertTrue(message = "Si el evento es de alimentación o vacunación, el costo debe ser mayor o igual que cero")
    public boolean isValidCosto() {
        if (tipo == null) {
            return true;
        }

        boolean requiereCosto =
                tipo == EventType.ALIMENTACION || tipo == EventType.VACUNACION;

        if (!requiereCosto) {
            return true;
        }

        return costo != null && costo.compareTo(BigDecimal.ZERO) >= 0;
    }
}
