# ? Chicken Suite ? Sistema de Gestión Avícola

### Proyecto Final ? Programación Orientada a Objetos (Java)

---

## ? Descripción general

**Chicken Suite** es una aplicación modular orientada a la **gestión de procesos avícolas**.  
Permite administrar especies, razas, lotes de producción, eventos diarios e indicadores económicos, usando interfaces generadas automáticamente con **OpenXava**, sobre un modelo de dominio diseñado con **POO**, **JPA/Hibernate** y buenas prácticas de diseño.

El sistema modela un escenario de producción real, integrando:

- Entidades de dominio claras y coherentes.
- Relaciones y multiplicidades entre especies, razas y lotes.
- Propiedades calculadas para apoyar la toma de decisiones.
- Lógica de negocio encapsulada en métodos de dominio.

---

## ? Objetivos del sistema

- Administrar **especies** y **razas**, incluyendo sus características productivas básicas.
- Registrar y dar seguimiento a **lotes de producción** desde su creación hasta el cierre.
- Registrar **eventos diarios** como alimentación, vacunación, limpieza y mortalidad.
- Generar **proyecciones económicas** a partir de:
    - Costos esperados.
    - Peso vivo esperado.
    - Precio estimado por kilogramo.
- Calcular indicadores productivos y económicos, tales como:
    - Mortalidad (%)
    - Tasa de aves vivas (%)
    - Ganancia diaria promedio por ave
    - Ingreso esperado
    - Margen económico esperado
    - Margen neto proyectado

---

## ? Módulos (entidades) del sistema

### 1. Species (Especie)

Gestiona las **especies avícolas** manejadas en la unidad productiva.

**Atributos principales:**

- `nombre`: nombre de la especie (por ejemplo, *Gallina*, *Codorniz*).
- `descripcion`: detalle breve o comentario general.

**Relaciones:**

- Una **Species** puede tener muchas **Breed** (razas).
- Una **Species** puede estar asociada a múltiples **FarmBatch** (lotes).

---

### 2. Breed (Raza)

Maneja las **razas específicas** dentro de cada especie.

**Atributos principales:**

- `nombre`: nombre de la raza (por ejemplo, *Isa Brown*).
- `especie`: referencia a la entidad `Species`.
- `proposito`: propósito productivo (`CARNE`, `HUEVO`, `DOBLE_PROPOSITO`).
- `diasCrecimientoEstandar`: días estándar estimados para llegar al peso objetivo.

**Relaciones:**

- Una raza pertenece a **una sola especie**.
- Una raza puede ser usada por **varios lotes** (`FarmBatch`).

**Método clave:**

- `getDiaCosechaEstimado()`  
  Devuelve el número de días estándar de crecimiento, útil para planificación en proyecciones y cronogramas.

---

### 3. FarmBatch (Lote)

Representa un **lote de aves** manejadas como una unidad productiva.

**Atributos principales:**

- `codigo`: identificador visible del lote (ej. `LOTE-ISA-001`).
- `especie`: especie general (ej. *Gallina*).
- `raza`: raza específica (ej. *Isa Brown*).
- `cantidadInicial`: número de aves al inicio.
- `cantidadVivaActual`: número de aves vivas actualmente.
- `pesoObjetivoGramos`: peso objetivo por ave, en gramos.
- `fechaInicio`: fecha de inicio del ciclo.
- `fechaFinPlaneada`: fecha planeada de cierre o venta.
- `etapa`: etapa productiva actual (`INCUBACION`, `CRIA`, `CRECIMIENTO`, `ENGORDE`, `VENDIDO`).
- `notas`: observaciones generales.

**Relaciones:**

- Cada lote pertenece a **una especie** y **una raza**.
- Un lote tiene **muchos eventos diarios** (`DailyEvent`).
- Un lote puede estar referenciado en **proyecciones económicas** (`Projection`) y **KPI** (`BatchKPI`).

**Propiedades calculadas (KPI productivos):**

- `getNumeroPerdidas()`  
  Aves perdidas = `cantidadInicial - cantidadVivaActual`.

- `getTasaMortalidadPorcentaje()`  
  Mortalidad (%) = (aves perdidas / cantidad inicial) × 100.

- `getTasaVivosPorcentaje()`  
  Aves vivas (%) = (cantidad viva actual / cantidad inicial) × 100.

- `getPesoVivoTotalActualKg()`  
  Peso vivo total estimado (kg) = `cantidadVivaActual × pesoObjetivoGramos ÷ 1000`.

- `getDiasTranscurridos()`  
  Días entre `fechaInicio` y la fecha actual.

- `getGananciaDiariaPromedioGr()`  
  Ganancia diaria promedio por ave (g/día) a partir de `pesoObjetivoGramos` y `diasTranscurridos`.

**Métodos de negocio (lógica de dominio):**

- `advanceStage()`  
  Avanza la etapa del lote en el orden:
  `INCUBACION ? CRIA ? CRECIMIENTO ? ENGORDE ? VENDIDO`.

- `registerEvent(DailyEvent evento)`  
  Agrega un evento al lote, completa datos faltantes (como fecha) y, si el evento es de mortalidad, descuenta las aves perdidas de `cantidadVivaActual`.

- `registerMortality(int cantidadAvesMuertas, String nota)`  
  Crea un evento de mortalidad, ajusta `cantidadVivaActual` y añade el evento a la colección del lote.

---

### 4. DailyEvent (Evento diario)

Registra **acciones o sucesos diarios** asociados a un lote.

**Tipos principales de evento (`EventType`):**

- `ALIMENTACION`
- `VACUNACION`
- `LIMPIEZA`
- `MORTALIDAD`

**Atributos:**

- `lote`: referencia al `FarmBatch`.
- `fecha`: fecha del evento (por defecto, la fecha actual).
- `tipo`: tipo de evento.
- `costo`: costo asociado (alimento, vacuna, servicio, etc.).
- `avesPerdidas`: número de aves muertas (solo si `tipo == MORTALIDAD`).
- `kilogramosAlimento`: kg de alimento suministrado (solo si `tipo == ALIMENTACION`).
- `notas`: observaciones.

**Relaciones:**

- Cada evento pertenece a **un único lote** (`FarmBatch`).

**Validaciones por tipo de evento:**

- Si el evento es de **mortalidad**, `avesPerdidas` debe ser > 0.
- Si el evento es de **alimentación**, `kilogramosAlimento` debe ser > 0.
- Si el evento es de **alimentación** o **vacunación**, `costo` debe ser ? 0.

Estas reglas se implementan mediante anotaciones como `@AssertTrue` e integran la validación de negocio directamente en el modelo.

---

### 5. Projection (Proyección económica)

Permite estimar la **rentabilidad económica** de un lote.

**Valores ingresados por el usuario:**

- `lote`: referencia al lote que se está proyectando.
- `precioEsperadoPorKg`: precio esperado por kilogramo de peso vivo.
- `costosEsperados`: costos totales esperados para el lote.

**Propiedades calculadas:**

- `getPesoVivoTotalEsperadoKg()`  
  Peso vivo total esperado (kg) usando:
  `cantidadVivaActual × pesoObjetivoGramos ÷ 1000` desde el lote.

- `getIngresoEsperado()`  
  Ingreso esperado = `pesoVivoTotalEsperadoKg × precioEsperadoPorKg`.

- `getMargenEsperado()`  
  Margen esperado = `ingresoEsperado ? costosEsperados`.

---

### 6. BatchKPI (Indicadores del lote)

Entidad enfocada en **indicadores clave de desempeño (KPI)** para cada lote.

**Atributos principales:**

- `lote`: referencia a `FarmBatch`.
- `gananciaDiariaPromedioGramos`: ganancia diaria promedio por ave (g/día).
- `indiceConversionAlimenticia`: FCR (Feed Conversion Ratio).
- `tasaMortalidadPorcentaje`: mortalidad %.
- `costoProyectado`: costo total proyectado.
- `ingresoProyectado`: ingreso total proyectado.

**Propiedad calculada:**

- `getMargenNeto()`  
  Margen neto proyectado = `ingresoProyectado ? costoProyectado`.

---

### 7. User (Usuario)

Administra los **usuarios internos** de Chicken Suite y sus permisos básicos.

**Atributos:**

- `nombreUsuario` (`username`): utilizado para iniciar sesión.
- `passwordHash` (`password_hash`): contraseña (en entorno real debería ser un hash seguro).
- `nombreCompleto` (`full_name`): nombre completo de la persona usuaria.
- `rol` (`rol`): rol funcional (`ADMINISTRADOR`, `ANALISTA`, `OPERADOR`).
- `activo` (`activo`): indica si la cuenta está activa.

**Lógica de permisos:**

- `puedeEditarCostos()`  
  Devuelve `true` solo si el usuario está activo y su rol es `ADMINISTRADOR`.

- `puedeVerReportes()`  
  Devuelve `true` si está activo y su rol es `ADMINISTRADOR` o `ANALISTA`.

**Normalización de datos:**

Antes de guardar:

- `nombreUsuario` se normaliza a minúsculas y sin espacios al inicio o final.
- `nombreCompleto` se guarda sin espacios sobrantes.

---

## ? Diseño UML general

El diseño UML del sistema contempla:

- Entidades modeladas con **JPA** y **OpenXava** (`@Entity`, `@View`, `@Tab`).
- Relaciones claras entre:
    - `Species` ? `Breed`
    - `Species`/`Breed` ? `FarmBatch`
    - `FarmBatch` ? `DailyEvent`
    - `FarmBatch` ? `Projection`
    - `FarmBatch` ? `BatchKPI`
- Enumeraciones (`EventType`, `Stage`, `Purpose`, `Role`) que definen:
    - Tipos de evento.
    - Etapas del lote.
    - Propósitos productivos.
    - Roles de usuario.
- Multiplicidades que reflejan la realidad del proceso productivo:
    - Una especie ? muchas razas.
    - Un lote ? muchos eventos.
    - Un lote ? varias proyecciones o registros de KPI.

---

## ? Propiedades calculadas

Las propiedades calculadas se implementan directamente en los getters, usando anotaciones como:

- `@ReadOnly`
- `@Depends("campo1, campo2, ...")`

Ejemplos:

- Mortalidad (%) = `(cantidadInicial - cantidadVivaActual) / cantidadInicial × 100`
- Aves vivas (%) = `cantidadVivaActual / cantidadInicial × 100`
- Ingreso esperado = `pesoVivoTotalEsperadoKg × precioEsperadoPorKg`
- Margen esperado = `ingresoEsperado ? costosEsperados`
- Margen neto = `ingresoProyectado ? costoProyectado`

Estas propiedades no se almacenan en la base de datos; se calculan cada vez que se consultan, garantizando información siempre actualizada.

---

## ?? Acciones y lógica de negocio

La lógica de negocio se encapsula en métodos de dominio como:

- `advanceStage()`  
  Controla el avance ordenado de etapas del lote.

- `registerEvent(DailyEvent evento)`  
  Registra un evento y ajusta el estado interno del lote si corresponde (por ejemplo, mortalidad).

- `registerMortality(int cantidadAvesMuertas, String nota)`  
  Facilita el registro rápido de mortalidad directamente desde la interfaz o desde otros servicios.

Estas operaciones se pueden exponer como **acciones en OpenXava**, permitiendo que el usuario final ejecute lógica de negocio desde la UI sin escribir código.

---

## ? Resumen

**Chicken Suite** es un sistema modular para la **gestión de producción avícola**, construido con:

- Un modelo de dominio claro y expresivo.
- Entidades anotadas con JPA y OpenXava.
- Propiedades calculadas para indicadores productivos y económicos.
- Lógica de negocio encapsulada en métodos bien definidos.
- Soporte para usuarios y roles internos.

Su diseño legible, extensible y alineado con los principios de Programación Orientada a Objetos lo convierte en una base sólida para gestionar especies, razas, lotes, eventos diarios y proyecciones económicas en una unidad avícola moderna.
