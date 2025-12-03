package com.tuempresa.chickenSuiteApp.enums;

/**
 * Rol funcional del usuario dentro de Chicken Suite.
 *
 * Se utiliza en User para controlar permisos básicos de edición y visualización.
 */
public enum Role {

    /**
     * Administrador del sistema.
     * Puede editar costos, proyecciones y consultar todos los reportes.
     */
    ADMINISTRADOR,

    /**
     * Productor o encargado de granja.
     * Enfocado en registrar lotes y eventos diarios.
     */
    PRODUCTOR,

    /**
     * Analista o personal de gestión que revisa indicadores y reportes.
     */
    ANALISTA
}
