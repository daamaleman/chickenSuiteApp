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
 * Evento diario asociado a un lote de producción.
 *
 * Ejemplos de uso:
 * - Alimentación (ALIMENTACION)
 * - Vacunación (VACUNACION)
 * - Limpieza (LIMPIEZA)
 * - Mortalidad (MORTALIDAD)
 *
 * La lógica que modifica el estado del lote (por ejemplo, descontar aves vivas)
 * se realiza desde la entidad FarmBatch mediante métodos de dominio.
 */
@Entity
@Table(name = "CS_DAILY_EVENT")
@Getter
@Setter
@View(members =
        "lote;" +
                "fecha, tipo;" +
                "kilogramosAlimento, avesPerdidas, costo;" +
                "notas"
)
@Tab(
        name = "Eventos diarios",
        properties = "lote.codigo, fecha, tipo, kilogramosAlimento, avesPerdidas, costo",
        defaultOrder = "fecha desc"
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
    private String oid;

    // =========================================================
    // Relación con el lote
    // =========================================================

    /**
     * Lote al que pertenece este evento.
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @DescriptionsList(descriptionProperties = "codigo, especie.nombre, raza.nombre")
    private FarmBatch lote;

    // =========================================================
    // Datos principales del evento
    // =========================================================

    /**
     * Fecha del evento.
     * Por defecto se utiliza la fecha actual al crear el registro.
     */
    @Required
    @DefaultValueCalculator(CurrentLocalDateCalculator.class)
    private LocalDate fecha;

    /**
     * Tipo de evento (alimentación, vacunación, limpieza, mortalidad, etc.).
     */
    @Enumerated(EnumType.STRING)
    @Required
    private EventType tipo;

    /**
     * Costo asociado al evento (alimento, vacuna, servicio, etc.).
     */
    @Money
    private BigDecimal costo;

    /**
     * Número de aves perdidas en este evento.
     * Solo aplica cuando el tipo es MORTALIDAD.
     */
    @Min(value = 0, message = "Las aves perdidas deben ser mayores o iguales a 0")
    private int avesPerdidas;

    /**
     * Kilogramos de alimento suministrados.
     * Solo aplica cuando el tipo es ALIMENTACION.
     */
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Los kilogramos de alimento deben ser mayores que 0"
    )
    private BigDecimal kilogramosAlimento;

    /**
     * Notas u observaciones adicionales del evento.
     */
    @TextArea
    private String notas;

    // =========================================================
    // Validaciones específicas por tipo de evento
    // =========================================================

    /**
     * Si el evento es de mortalidad, debe indicar cuántas aves murieron.
     */
    @AssertTrue(message = "Si el evento es de mortalidad, debe indicar cuántas aves murieron")
    public boolean esValidoAvesPerdidas() {
        if (tipo != EventType.MORTALIDAD) {
            return true;
        }
        return avesPerdidas > 0;
    }

    /**
     * Si el evento es de alimentación, debe indicar los kilogramos de alimento.
     */
    @AssertTrue(message = "Si el evento es de alimentación, debe ingresar los kilogramos de alimento suministrados")
    public boolean esValidoKilogramosAlimento() {
        if (tipo != EventType.ALIMENTACION) {
            return true;
        }
        return kilogramosAlimento != null &&
                kilogramosAlimento.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Si el evento es de alimentación o vacunación, el costo debe ser mayor o igual que cero.
     * Para otros tipos de evento no se aplica esta restricción.
     */
    @AssertTrue(message = "Si el evento es de alimentación o vacunación, el costo debe ser mayor o igual que cero")
    public boolean esValidoCosto() {
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
