package com.tuempresa.chickenSuiteApp.enums;

/**
 * Tipo de evento diario registrado para un lote.
 *
 * Se utiliza en DailyEvent aplicar validaciones específicas y lógica de negocio.
 */
public enum EventType {

    // Evento de alimentación: registro de alimento suministrado.
    ALIMENTACION,

    // Evento de mortalidad: aves que mueren en una fecha determinada.
    MORTALIDAD,

    // Evento de vacunación u otra intervención sanitaria.
    VACUNACION,

    // Evento de limpieza o desinfección de instalaciones.
    LIMPIEZA,

    // Otro tipo de evento no clasificado explícitamente.
    OTRO
}
