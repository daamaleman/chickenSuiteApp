package com.tuempresa.chickenSuiteApp.modelo;

import com.tuempresa.chickenSuiteApp.enums.EventType;
import com.tuempresa.chickenSuiteApp.enums.Stage;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.openxava.annotations.*;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Lote de producción: conjunto de aves gestionadas como una unidad.
 *
 * Reglas importantes:
 * - La cantidad de aves vivas solo debe modificarse mediante los métodos
 *   de dominio (advanceStage, registerEvent, registerMortality).
 * - Los campos calculados se exponen como propiedades @ReadOnly para la UI.
 */
@Entity
@Getter
@Setter
@View(members =
        "codigo, especie, raza;" +
                "cantidadInicial, cantidadVivaActual, numeroPerdidas, tasaMortalidadPorcentaje;" +
                "pesoObjetivoGramos, pesoVivoTotalActualKg, diasTranscurridos, gananciaDiariaPromedioGr;" +
                "etapa, fechaInicio, fechaFinPlaneada;" +
                "notas;" +
                "eventos"
)
@Tab(properties =
        "codigo, especie.nombre, raza.nombre, " +
                "cantidadVivaActual, numeroPerdidas, tasaMortalidadPorcentaje, pesoVivoTotalActualKg"
)
public class FarmBatch {

    // =========================================================
    // Identificador
    // =========================================================

    @Id
    @Hidden
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    @Column(length = 32)
    // Identificador único interno (UUID)
    private String oid;

    // =========================================================
    // Datos básicos del lote
    // =========================================================

    @Column(length = 20, unique = true)
    @Required
    @NotBlank(message = "El código del lote es obligatorio")
    // Código visible para el usuario (ej. LOTE-ISA-001)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList
    // Especie general (Pollo, Codorniz, etc.)
    private Species especie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @DescriptionsList
    // Raza específica dentro de la especie (Isa Brown, Ross 308, etc.)
    private Breed raza;

    // =========================================================
    // Valores productivos
    // =========================================================

    @Min(value = 1, message = "La cantidad inicial debe ser al menos 1")
    // Número de aves al iniciar el lote
    private int cantidadInicial;

    @Min(value = 0, message = "La cantidad de aves vivas no puede ser negativa")
    // Número actual de aves vivas
    private int cantidadVivaActual;

    @Min(value = 1, message = "El peso objetivo por ave debe ser mayor que 0")
    // Peso objetivo por ave en gramos (ej. 1800 gr para pollos de engorde)
    private int pesoObjetivoGramos;

    // Fechas del ciclo del lote
    private LocalDate fechaInicio;
    private LocalDate fechaFinPlaneada;

    @Enumerated(EnumType.STRING)
    // Etapa productiva actual: INCUBACION, CRIA, CRECIMIENTO, ENGORDE, VENDIDO
    private Stage etapa;

    @TextArea
    // Notas de manejo, comentarios del productor
    private String notas;

    // =========================================================
    // Eventos diarios asociados al lote
    // =========================================================

    @OneToMany(mappedBy = "lote")
    @ListProperties("fecha, tipo, kilogramosAlimento, avesPerdidas, costo")
    // Registro de alimentación, vacunación, limpieza, mortalidad, etc.
    private Collection<DailyEvent> eventos;

    // =========================================================
    // Propiedades calculadas para análisis rápido
    // =========================================================

    /**
     * Porcentaje de mortalidad respecto a la cantidad inicial de aves.
     * Ejemplo: 10 aves perdidas de 100 iniciales = 10.00 %
     */
    @ReadOnly
    @Depends("cantidadInicial, cantidadVivaActual")
    public BigDecimal getTasaMortalidadPorcentaje() {
        int perdidas = getNumeroPerdidas();
        return calcularPorcentaje(perdidas, cantidadInicial);
    }

    /**
     * Porcentaje de aves vivas respecto a la cantidad inicial.
     * Ejemplo: 90 aves vivas de 100 iniciales = 90.00 %
     */
    @ReadOnly
    @Depends("cantidadInicial, cantidadVivaActual")
    public BigDecimal getTasaVivosPorcentaje() {
        return calcularPorcentaje(cantidadVivaActual, cantidadInicial);
    }

    /**
     * Peso vivo total actual estimado en kilogramos.
     * Fórmula: cantidadVivaActual * pesoObjetivoGramos / 1000.
     */
    @ReadOnly
    @Depends("cantidadVivaActual, pesoObjetivoGramos")
    public BigDecimal getPesoVivoTotalActualKg() {
        if (cantidadVivaActual <= 0 || pesoObjetivoGramos <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal aves = BigDecimal.valueOf(cantidadVivaActual);
        BigDecimal pesoGramos = BigDecimal.valueOf(pesoObjetivoGramos);
        BigDecimal totalGramos = aves.multiply(pesoGramos);

        return totalGramos.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    /**
     * Número absoluto de aves perdidas desde el inicio del lote.
     */
    @ReadOnly
    @Depends("cantidadInicial, cantidadVivaActual")
    public int getNumeroPerdidas() {
        if (cantidadInicial <= 0) {
            return 0;
        }
        return Math.max(0, cantidadInicial - cantidadVivaActual);
    }

    /**
     * Días transcurridos desde la fecha de inicio hasta hoy.
     * Si no hay fecha de inicio, devuelve 0.
     */
    @ReadOnly
    @Depends("fechaInicio")
    public long getDiasTranscurridos() {
        if (fechaInicio == null) {
            return 0;
        }
        long dias = ChronoUnit.DAYS.between(fechaInicio, LocalDate.now());
        return Math.max(dias, 0);
    }

    /**
     * Ganancia diaria promedio esperada por ave, en gramos.
     * Fórmula aproximada: pesoObjetivoGramos / diasTranscurridos.
     */
    @ReadOnly
    @Depends("pesoObjetivoGramos, fechaInicio")
    public BigDecimal getGananciaDiariaPromedioGr() {
        long diasTranscurridos = getDiasTranscurridos();

        if (diasTranscurridos <= 0 || pesoObjetivoGramos <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal pesoObjetivo = BigDecimal.valueOf(pesoObjetivoGramos);
        return pesoObjetivo.divide(
                BigDecimal.valueOf(diasTranscurridos),
                2,
                RoundingMode.HALF_UP
        );
    }

    // =========================================================
    // Lógica de negocio pública (métodos de dominio)
    // =========================================================

    /**
     * Avanza la etapa del lote a la siguiente fase del ciclo productivo.
     * Si la etapa es nula, se inicializa en INCUBACION.
     * Si ya está en VENDIDO, no cambia.
     */
    public void advanceStage() {
        if (etapa == null) {
            etapa = Stage.INCUBACION;
            return;
        }

        switch (etapa) {
            case INCUBACION:
                etapa = Stage.CRIA;
                break;
            case CRIA:
                etapa = Stage.CRECIMIENTO;
                break;
            case CRECIMIENTO:
                etapa = Stage.ENGORDE;
                break;
            case ENGORDE:
                etapa = Stage.VENDIDO;
                break;
            case VENDIDO:
                // Etapa final: no se avanza más
                break;
        }
    }

    /**
     * Registra un evento asociado al lote.
     * - Asigna este lote al evento.
     * - Si el evento es de mortalidad, descuenta las aves perdidas
     *   de la cantidad de aves vivas.
     * - Si la fecha del evento viene nula, se completa con la fecha actual.
     */
    public void registerEvent(DailyEvent evento) {
        if (evento == null) {
            return;
        }

        if (evento.getFecha() == null) {
            evento.setFecha(LocalDate.now());
        }

        evento.setLote(this);

        if (evento.getTipo() == EventType.MORTALIDAD) {
            aplicarMortalidadDesdeEvento(evento);
        }

        agregarEventoALote(evento);
    }

    /**
     * Crea y registra un evento de mortalidad.
     * Este método es una forma simplificada de registrar pérdida de aves
     * desde la interfaz o desde otras capas de la aplicación.
     */
    public DailyEvent registerMortality(int cantidadAvesMuertas, String nota) {
        if (cantidadAvesMuertas <= 0) {
            return null;
        }

        DailyEvent evento = new DailyEvent();
        evento.setTipo(EventType.MORTALIDAD);
        evento.setAvesPerdidas(cantidadAvesMuertas);
        evento.setNotas(nota);
        evento.setFecha(LocalDate.now());

        // Se delega al método general para mantener una sola lógica
        registerEvent(evento);

        return evento;
    }

    // Alias en inglés para uso en otras partes del modelo, si es necesario

    public BigDecimal getMortalityRatePct() {
        return getTasaMortalidadPorcentaje();
    }

    public BigDecimal getAliveRatePct() {
        return getTasaVivosPorcentaje();
    }

    // =========================================================
    // Métodos privados de apoyo
    // =========================================================

    /**
     * Calcula un porcentaje con dos decimales: (parte / total) * 100.
     * Si el total es cero o negativo, devuelve 0.00.
     */
    private BigDecimal calcularPorcentaje(int parte, int total) {
        if (total <= 0 || parte < 0) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(parte)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Aplica el efecto de un evento de mortalidad sobre la cantidad de aves vivas.
     * Nunca permite que la cantidad de aves vivas sea negativa. Si el número
     * de aves perdidas es mayor que las aves vivas actuales, se ajusta.
     */
    private void aplicarMortalidadDesdeEvento(DailyEvent evento) {
        int avesPerdidas = evento.getAvesPerdidas();

        if (avesPerdidas <= 0 || cantidadVivaActual <= 0) {
            return;
        }

        if (avesPerdidas > cantidadVivaActual) {
            avesPerdidas = cantidadVivaActual;
            evento.setAvesPerdidas(avesPerdidas);
        }

        cantidadVivaActual = cantidadVivaActual - avesPerdidas;
    }

    /**
     * Asegura que la colección de eventos esté inicializada antes de agregar.
     */
    private void agregarEventoALote(DailyEvent evento) {
        if (eventos == null) {
            eventos = new ArrayList<>();
        }
        eventos.add(evento);
    }
}
