package com.tuempresa.chickenSuiteApp.enums;

/**
 * Fases del ciclo de un lote de aves.
 *
 * Se utiliza en FarmBatch para indicar en qué etapa productiva se encuentra el lote.
 */
public enum Stage {

    // Etapa inicial: llegada de pollitos o inicio de levante.
    INCUBACION,

    // Fase de cría/levante temprano.
    CRIA,

    // Fase intermedia de crecimiento.
    CRECIMIENTO,

    // Fase de engorde previo a la venta o sacrificio.
    ENGORDE,

    // El lote ya fue vendido o cerrado.
    VENDIDO
}
