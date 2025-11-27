---

# ? **Chicken Suite ? Sistema de Gestión Avícola**

### Proyecto Final ? Programación Orientada a Objetos (Java)

---

## ? **Descripción General**

**Chicken Suite** es una aplicación modular orientada a la gestión de procesos avícolas. Permite administrar especies, razas, lotes de producción, eventos diarios y proyecciones económicas mediante interfaces generadas automáticamente con **OpenXava**, siguiendo principios sólidos de **Programación Orientada a Objetos (POO)**.

El sistema se basa en un modelo de dominio claro y extensible, diseñado para representar escenarios reales de producción avícola, con lógica de negocio integrada y propiedades calculadas para facilitar la toma de decisiones.

---

## ? **Objetivos del Sistema**

* Administrar **especies** y **razas**, incluyendo características productivas.
* Registrar y dar seguimiento a **lotes** desde su creación hasta su cierre.
* Registrar **eventos diarios** como alimentación, vacunación, limpieza y mortalidad.
* Generar **proyecciones económicas** basadas en costo, peso vivo estimado y precio de venta.
* Calcular indicadores productivos claves (KPI), como:

    * Mortalidad
    * Tasa de aves vivas
    * Ganancia diaria promedio
    * Ingreso proyectado
    * Margen económico

---

## ?? **Módulos del Sistema**

### ? **1. Species (Especie)**

Gestión de las especies presentes en la unidad productiva.

**Atributos principales:**

* `name`: nombre de la especie
* `description`: detalle o comentarios

**Relaciones:**

* Una especie puede tener múltiples razas y múltiples lotes.

---

### ? **2. Breed (Raza)**

Maneja las razas específicas dentro de cada especie.

**Incluye:**

* Propósito productivo (MEAT, EGG, DUAL).
* Días estándar de crecimiento (`growthDaysStd`).

**Relaciones:**

* Una raza pertenece a una especie.
* Una raza puede ser usada por varios lotes.

**Método clave:**

* `expectedHarvestDay()`: devuelve el estándar de días hasta cosecha.

---

### ? **3. FarmBatch (Lote)**

Representa un lote de aves criadas en conjunto.

**Atributos principales:**

* Cantidad inicial y actual
* Fechas de inicio y fin planeado
* Peso objetivo
* Etapa actual (INCUBATION ? SOLD)
* Notas operativas

**Relaciones:**

* Pertenece a una especie y a una raza.
* Contiene múltiples eventos diarios (`DailyEvent`).
* Puede tener una proyección económica.

**Métodos clave:**

* `advanceStage()`: avanza el lote a la siguiente fase.
* `registerEvent(DailyEvent)`: agrega un evento al lote.
* `registerMortality(int, String)`: descuenta aves vivas y crea un evento asociado.

**Propiedades calculadas (KPI):**

* `getMortalityRatePct()`: mortalidad %
* `getAliveRatePct()`: aves vivas %

---

### ? **4. DailyEvent (Evento Diario)**

Registro de actividades o sucesos importantes en el lote.

**Tipos permitidos (`EventType`):**

* FEEDING
* VACCINATION
* CLEANING
* MORTALITY

**Atributos:**

* Fecha del evento
* Tipo
* Kilogramos de alimento (`feedKg`)
* Mortalidad (`deadCount`)
* Costo del evento
* Notas

**Relaciones:**

* Cada evento pertenece a un único lote.

---

### ? **5. Projection (Proyección Económica)**

Permite calcular la rentabilidad estimada del lote.

**Valores ingresados:**

* Precio por kg (`expectedPricePerKg`)
* Costo total estimado (`expectedCosts`)
* Peso vivo estimado (`expectedLiveWeightKg`)

**Propiedades calculadas:**

* `getExpectedIncome()`: ingreso esperado
* `getExpectedMargin()`: margen esperado

---

### ? **6. BatchKPI (Métricas del Lote)**

Módulo orientado a representar indicadores productivos clave.

**Atributos:**

* Ganancia diaria promedio (`avgDailyGainGr`)
* Conversión alimenticia (`fcr`)
* Mortalidad (%)
* Ingresos y costos proyectados

**Propiedad calculada:**

* `getNetMargin()`: margen neto

---

### ? **7. User (Usuario)**

Encargado de la gestión básica de usuarios del sistema y permisos.

**Atributos:**

* `username`
* `passwordHash`
* `fullName`
* `role` (ADMIN, PRODUCER, ANALYST)
* `active`

**Métodos:**

* `canEditCosts()`
* `canViewReports()`

---

## ? **Diseño UML General**

El diseño UML del sistema contempla:

* Entidades modeladas con JPA y OpenXava.
* Relaciones entre Species, Breed, FarmBatch, DailyEvent, Projection, BatchKPI y User.
* Enumeraciones que definen estados, roles y categorías.
* Multiplicidades claras que reflejan la estructura real del proceso productivo.
* Propiedades derivadas (calculadas) implementadas directamente en getters con `@Depends`.

---

## ? **Propiedades Calculadas**

Las propiedades calculadas permiten obtener resultados dinámicos sin almacenarlos:

* Mortalidad (%) = (inicial ? actual) / inicial × 100
* Aves vivas (%) = actual / inicial × 100
* Ingreso proyectado = peso estimado × precio por kg
* Margen proyectado = ingreso ? costos

Se implementan con anotaciones:

* `@ReadOnly`
* `@Depends("campo1, campo2")`

---

## ?? **Acciones y Lógica de Negocio**

El modelo incorpora métodos con lógica del dominio, por ejemplo:

* Avance de etapa (`advanceStage()`)
* Registro de mortalidad (`registerMortality()`)
* Cálculo de indicadores y proyecciones

Estos métodos pueden exponerse como **acciones desde la interfaz OpenXava**, permitiendo interacción directa con la lógica del sistema.

---

## ? **Resumen**

Chicken Suite es un sistema modular para la administración de producción avícola que combina un modelo de dominio profesional, lógica de negocio encapsulada, cálculos clave y módulos generados automáticamente con OpenXava. Su diseño limpio, extensible y basado en POO lo convierte en un prototipo sólido para el manejo real de lotes, razas, especies y proyecciones económicas en una unidad avícola moderna.

---