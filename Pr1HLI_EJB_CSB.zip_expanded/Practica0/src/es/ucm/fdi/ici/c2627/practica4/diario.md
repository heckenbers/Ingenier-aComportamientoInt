# Diario · Práctica 4 · grupoHLI_CSB_EJB

**Integrantes:** Elkin Joel Benitez Castillo · Carlos Salvador Buhigas de las Heras · Héctor López Ibáñez

**Concurso:** 
 - **MsPacMan:** con `mspacman-v5.fcl` (16 reglas): es la que carga `MsPacMan()`.
 - **Ghosts:** en desarrollo; provisionalmente `ghosts-v0.fcl` (2 reglas) por tener menor media de puntos concedidos que `v1` mientras iteramos.

# Parte 1 · Implementación de Ms. Pac-Man
**Rivales:** los cinco fantasmas de referencia (Random, Aggressive, Basico, Intermedio, Avanzado) más los fantasmas de Héctor de la Práctica 2 (*P2 Hector*), fijados desde el principio. 300 partidas por rival, semillas 1–300, límite de 4000 turnos.
**Métricas:** puntos, nivel alcanzado, turnos por vida, fantasmas comidos.
**La vara:** la V3 de Carlos de la Práctica 2 (*P2 V3*, una HFSM), que era nuestra mejor Ms. Pac-Man: 5755 puntos de media contra estos seis rivales. El objetivo era una borrosa que la iguale y después la supere.

**Cómo está hecho.** La percepción y las acciones vienen de la V3 de la P2, en Java:

- `Percepcion` calcula la amenaza de cada fantasma, las rutas de huida, el plan de caza y el cebo. Es el antiguo `MsPacManInput`, sin estado estático.
- Las acciones son los estados hoja de la HFSM:

  | Acción | Estado de la V3 |
  |---|---|
  | `recolectar` | IrAPildoraRentable |
  | `evadir` | Evadir |
  | `escapar` | EscapeDeEmergencia |
  | `cazar` | CazarFantasma |
  | `esperarCebo` | la patrulla de PrepararCebo |
  | `comerCebo` | ir a por la pill del cebo |

Lo que antes decidían las transiciones de la HFSM ahora lo decide el FCL. `MsPacManInput` le pasa siete números:

| Entrada | Qué mide | Rango |
|---|---|---|
| `amenaza` | ticks hasta que el fantasma peligroso más rápido llega a Ms. Pac-Man | 0..60 |
| `margen` | ventaja de la mejor ruta de huida sobre todos los fantasmas | −10..40 |
| `caza` | fantasmas comestibles que da tiempo a encadenar | 0..4 |
| `revertir` | ticks hasta que un comestible vuelva a ser peligroso a su lado | 0..60 |
| `cebo` | fantasmas que encadenaría comiéndose ahora la pill del cebo | 0..4 |
| `espera` | ticks seguidos esperando junto al cebo | 0..120 |
| `quedan` | % de píldoras que quedan | 0..100 |

Todas las salidas van de 0 a 10, con `poco`, `algo` y `mucho`. Gana la más alta con `MaxActionSelector`; en un empate, la más prudente, por el orden del array: escapar, evadir, cazar, comerCebo, esperarCebo, recolectar.

**Herramientas.**

- `depurar/ProbarBorroso` toma la versión como argumento. Cada caso parte de «no pasa nada» y cambia solo lo que importa. Los casos de una versión se comprueban también en todas las siguientes. Además avisa de **EMPATE** si las dos salidas más altas están a menos de 0,2, aunque el caso salga BIEN.
- La traza es la `TrazaBorrosa` del ejemplo, enganchada desde fuera del paquete; no va en la entrega.

> jFuzzyLogic solo acepta **dos consecuencias por regla**: con tres, el .fcl no carga (`mismatched input ','`). Cuando una situación sube una salida y baja otras dos, son dos reglas con la misma condición.

---

## lun 16 nov · v0: la raíz de la V3, borrosa (10 reglas)

**Qué hace.** Escapar cuando el fantasma está encima o no queda ruta con ventaja (R1, R2). Evadir cuando se acerca (R3, R4). Recolectar cuando no hay nadie cerca, más cuanto más margen (R5–R7). Cazar si hay comestibles a tiro y no van a volver pronto (R8, R9). Apartarse de un comestible que está a punto de volver (R10). Todavía sin cebo.

**ProbarBorroso.**

- El primer intento tenía un caso MAL: *«uno a tiro, pero vuelve en 5 ticks: evade»* ganaba `recolectar`, porque R5 subía `recolectar` aunque un comestible estuviera a punto de volver. **Arreglo:** R5 y R7 exigen `revertir IS tarde`.
- La curva de `margen` tenía un **empate a 5,0** entre 8 y 11 ticks de margen: R7 subía `evadir` y `recolectar` a `algo` a la vez. **Arreglo:** R7 sube `recolectar` y baja `evadir`.

Ahora son 8/8 BIEN, sin empates. Curva de `amenaza` (margen holgado): escapa hasta 4 ticks, evade entre 8 y 12, y a partir de 16 recolecta. No hay huecos.

**Comparar (v0 frente a P2 V3).**

```
MEDIA (6)  puntos   5755 ± 55  →  4657 ± 46   -1098   2·ET 143  peor
v0 es mejor contra 0 rivales, peor contra 4 y no se distingue contra 2.
           nivel    0.204 → 0.128 peor · turnos por vida 2928 → 3030 mejor · fantasmas comidos 7.90 → 5.99 peor
```

Era lo esperado: es la V3 sin cebo. En la P2 medimos que el cebo valía +1058.

---

## lun 16 nov · v1: + el cebo (15 reglas)

**Qué cambia.**

- **R11:** con un cebo de dos fantasmas y nadie cerca, esperar junto a la pill.
- **R12, R13:** con tres o más fantasmas, o cuando la espera ya es larga, comérsela.
- **R14:** si el cebo no vale, ni esperar ni ir a por él.
- **R15:** mientras se espera, comérsela vale poco.

Dos acciones nuevas: `esperarCebo` y `comerCebo`.

**ProbarBorroso.** Seis casos nuevos; los 14 dan BIEN.

- La curva de `espera` cambiaba de esperar a comer a los 45–50 ticks. Moví los términos (`poca` hasta 95, `mucha` desde 65) para que el cambio caiga hacia los 70–80, que es la paciencia de la V3.
- La misma curva tenía un **hueco**: `comerCebo` valía 0 (su DEFAULT) mientras se esperaba. No cambiaba la ganadora, pero se cierra con R15.
- La curva de `cebo` avisa de un EMPATE en cebo = 1,5. Es el punto de cruce entre recolectar y esperar, y la entrada solo vale 1, 2, 3 o 4 en el juego (200, 600, 1400 o 3000 puntos), así que no pasa nunca.

**Traza** (Aggressive, semilla 7: 7060 puntos):

- Gana `recolectar` en 3004 de 4001 turnos, `cazar` en 504, `escapar` en 201, `evadir` en 164, `comerCebo` en 101 y `esperarCebo` en 27.
- **RULE 10 no se activa NUNCA**, ni contra Aggressive ni contra Avanzado. Con la traza turno a turno, `revertir` no baja de unos 40 ticks en toda la partida. Ms. Pac-Man se come los comestibles, o los deja, mucho antes de que vuelvan a ser peligrosos a su lado: el plan de caza de la V3 ya descarta los que no llega a comer. La regla atiende una situación que no se da.

**Tiempo.** El primer turno tardaba unos 470 ms, por el preproceso del laberinto. Ahora los cuatro mapas se calculan en el constructor y el primer turno baja a lo normal. Esto no cambia ninguna decisión.

**Comparar (v1 frente a v0).**

```
MEDIA (6)  puntos   4657 ± 46  →  5713 ± 55   +1056   2·ET 143  mejor
v1 es mejor contra 5 rivales, peor contra 0 y no se distingue contra 1.
           nivel 0.128 → 0.198 mejor · turnos por vida 3030 → 2943 peor · fantasmas comidos 5.99 → 7.96 mejor
```

**v1 frente a la vara:** 5713 frente a 5755 de la V3, con las mismas partidas. D = −42 y 2·ET_D = 156: **ruido**. La borrosa ya juega como la HFSM.

Contra Héctor saca 3884 frente a 3625 de la V3. Contra Avanzado no cambia nada: ni el cebo (−3) ni nada de lo anterior le afecta.

---

## lun 16 nov · v2: con margen holgado, un fantasma cerca no hace huir (16 reglas) → descartada

**Problema.** La V3 era demasiado prudente: contra Random sobrevivía hasta el límite de turnos pero acababa pocos laberintos. La v1 hereda su regla: cualquier fantasma a menos de unos 12 ticks la hace evadir, aunque su mejor ruta saque mucha ventaja. Creemos que pierde tiempo huyendo de fantasmas que no la alcanzan.

**Cambio.** R3 solo evade con margen `justo`. La nueva R16, con margen `holgado`, sube `recolectar` y deja `evadir` en `algo`. Así decide con dos entradas a la vez, y no con un umbral de distancia.

**ProbarBorroso.** Dos casos nuevos: «fantasma a 10, margen 25 → recolecta» y «margen 10 → evade»; los 16 dan BIEN. La curva de `margen` con el fantasma a 10 ticks cruza suave: escapa hasta 5 de margen, evade de 5 a 14, y a partir de 17 recolecta (`evadir` baja de 8,6 a 5,0 y `recolectar` sube de 1,4 a 8,6). No hay huecos.

**Comparar (v2 frente a v1).**

```
MEDIA (6)  puntos   5706 ± 54  →  4995 ± 53   -711   2·ET 151  peor
v2 es mejor contra 0 rivales, peor contra 3 y no se distingue contra 3.
           nivel ruido · turnos por vida 2912 → 3004 mejor · fantasmas comidos 7.92 → 6.79 peor
```

Vive algo más, pero come muchos menos fantasmas: contra Aggressive pasa de 11,1 a 7,4, y contra Intermedio de 8,5 a 6,3.

**Por qué creemos que pasa.** Esperando junto al cebo, cuando el fantasma por fin se acercaba, en la v1 ganaba `evadir`. `Evadir` prefiere las rutas que pasan por la pill del cebo, y así se la comía con los fantasmas encima: la cadena entera. En la v2 gana `recolectar`, que penaliza pisar las power pills, y se aleja del cebo justo cuando valía la pena. **Se descarta.**

---

## lun 16 nov · v3: + comerse el cebo cuando el fantasma ya viene (18 reglas, sobre la v2)

**Cambio.** Si la explicación anterior es cierta, hay que comerse el cebo cuando el fantasma se acerca, de forma explícita. R17 sube `comerCebo` con amenaza `cerca` y un cebo de dos o más fantasmas. La pill del cebo solo se elige si Ms. Pac-Man llega antes que nadie, así que es seguro. R18 baja `esperarCebo` y `recolectar`.

**ProbarBorroso.** Tres casos nuevos.

- El primer intento dejaba MAL *«cebo de 2 con fantasma a 10: se lo come»*: ganaba `evadir` 5,9 a 4,9. Las reglas activas lo explicaban: R15 (de la v1) bajaba `comerCebo` mientras se esperaba y tapaba a R17. **Arreglo:** R15 solo con el fantasma lejos.
- Después, *«cebo de 3 con fantasma a 10 y margen 25»* salía BIEN pero con **EMPATE**: `comerCebo` 8,6 contra `recolectar` 8,6, que subía la R16 de la v2. **Arreglo:** R18 también baja `recolectar`. Ahora `comerCebo` 8,6 y `recolectar` 5,0.

Los 18 dan BIEN.

**Comparar (v3 frente a v2).**

```
MEDIA (6)  puntos   4995 ± 53  →  5646 ± 51   +651   2·ET 147  mejor
v3 es mejor contra 3 rivales, peor contra 0 y no se distingue contra 3.
           fantasmas comidos 6.79 → 7.91 mejor
```

Recupera los fantasmas comidos, lo que confirma la explicación de la v2. Pero frente a la v1 (5706, en la misma tanda) se queda en −60, que es ruido: la v3 es «v2 + un arreglo». **La prueba buena es la regla de la v3 sobre la v1.** Esa es la v4.

**Traza** (resumen de 5 partidas contra cada rival):

- RULE 10 se activa en 10 de 104 146 turnos. Está prácticamente muerta, como ya se vio en la v1.
- Todas las demás reglas se activan.
- Gana `recolectar` en el 46 % de los turnos, `cazar` en el 17 %, `evadir` en el 16 % y `escapar` en el 11 %.

**Muertes** (10 partidas contra Avanzado y 10 contra Héctor): las 52 muertes ocurren con `escapar`. Mirando los 30 turnos anteriores, el `margen` lleva mucho rato en torno a 0 mientras el fantasma está aún a 25 o 30 ticks: la trampa se cierra mucho antes de que llegue. Eso no lo arregla elegir otra acción: es un límite de la búsqueda de rutas de `escapar`, heredada de la V3. Lo dejamos apuntado como siguiente línea de trabajo.

---

## lun 16 nov · v4: la regla de la v3, sobre la v1 (17 reglas)

**Cambio.** El de la v3 (R17, R18 y R15 solo con el fantasma lejos), pero partiendo de la v1, sin la R16 de la v2.

**ProbarBorroso.** Al descartar la v2, las versiones ya no van en línea recta. Ahora cada caso dice en qué versiones vale:

- `"0-"`: de la v0 en adelante.
- `"1,2"`: solo esas dos.
- `"2,3"`: los casos de la v2, que solo valen en las versiones que salen de ella.

El caso de la v1 *«cebo de 2 con fantasma a 10: evade»* pasa a ser *«se lo come»* a propósito. Los 17 dan BIEN.

**Comparar (v4 frente a v1, misma tanda).**

```
MEDIA (6)  puntos   5595 ± 53  →  5672 ± 49   +77   2·ET 145  ruido
v4 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 5.
```

Es mejor contra Héctor (+238, con un umbral de 189); el resto, ruido. Fantasmas comidos: de 7,78 a 7,97, también ruido.

La v1 ha salido 5713, 5706 y 5595 en tres tandas con las mismas semillas. La diferencia viene de la posición inicial al azar del motor (`Math.random()`), igual que en la P2. Por eso comparamos siempre dentro de la misma tanda. **Se queda**, porque no empeora nada y gana a Héctor, pero sin pruebas de que mejore la media.

---

## lun 16 nov · v5: sin la regla 10 (16 reglas)

**Cambio.** Fuera R10 («apartarse de un comestible a punto de volver»). En la traza no se activa nunca, porque `revertir` no baja de unos 40 ticks. La entrada se sigue declarando y el caso de R10 en ProbarBorroso solo vale hasta la v4. Los 16 dan BIEN.

**Comparar (v5 frente a v4).**

```
MEDIA (6)  puntos   5672 ± 49  →  5672 ± 51   +0   2·ET 142  ruido
v5 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 6.
```

Exactamente igual de media, como se esperaba de una regla que no se activaba. **Se queda:** hace lo mismo con una regla menos, y en la versión elegida no puede haber reglas que no se activen.

---

## lun 16 nov · v6: + ir derecha a la píldora más cercana (18 reglas, una entrada y una acción nuevas)

**Problema** (traza turno a turno, v5 contra Random, semillas 3 y 5).

- Llega a 4000 turnos con las tres vidas y sin acabar el primer laberinto.
- Desde el turno 2000, `quedan` no se mueve (6,4 % en una partida y 16,4 % en la otra) mientras gana `recolectar` en casi todos los turnos. Lleva 2000 turnos dando vueltas sin comer.
- **Por qué:** `recolectar` (de la V3) puntúa cada píldora por 1/distancia. Cuando las que quedan están lejos, pesa más el término del margen y se va a las zonas seguras. Además, las píldoras de un callejón tienen una penalización de 200.
- La V3 tenía el mismo fallo: contra Random sacaba unos 4900 puntos, vivía hasta el límite y solo acababa el 42 % de los primeros laberintos.

**Cambio.**

- Una entrada nueva, `pildora`: pasos hasta la píldora normal más cercana, de 0 a 100. Se declara también en los .fcl de la v0 a la v5, sin reglas, para que no fallen con *No such variable*; eso no cambia lo que hacían.
- Una acción nueva, `irAPildora`: derecha a esa píldora por una ruta segura (`Navegacion.irA`). `recolectar` no se toca, porque la usan las versiones anteriores.
- **R19:** con la píldora lejos, sin peligro, sin cebo y sin nada que cazar, subir `irAPildora` y bajar `recolectar`.
- **R20:** con una píldora cerca, `irAPildora` vale poco.

**ProbarBorroso.** Cuatro casos nuevos: píldora a 40 → `irAPildora`; con fantasma a 10 → `evadir`; con cebo de 2 → `esperarCebo`; con dos a tiro → `cazar`. Los 20 dan BIEN. La curva de `pildora` cambia de `recolectar` a `irAPildora` hacia los 18 pasos, sin huecos.

**Un hueco que queda, para la siguiente.** En la curva de `espera` (cebo de 2), hacia los 80 ticks, gana `recolectar` (5,8) entre esperar y comer (5,0 y 5,0). Es la R15 de la v1, que añadí después de mirar esa curva, y está en todas las versiones desde la v1. Arreglarlo sería otra versión.

**Comparar (v6 frente a v5, en la tanda de la vara).**

```
MEDIA (6)  puntos   5672 ± 50  →  5644 ± 49   -27   2·ET 139  ruido
v6 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 6.
```

Contra Random casi mejora (+261, con un umbral de 275); en el resto no cambia.

**Traza** (v6 contra Random). La regla nueva se activa mucho: `irAPildora` gana en unos 1400–1600 turnos por partida. En la semilla 3 sí sirve: pasa de 5060 a 9370 puntos y acaba el primer laberinto. Pero en la semilla 5 `quedan` sigue parado en el 5 % durante 1500 turnos mientras «va a por la píldora».

Creemos que es un problema de Java, no de las reglas. `irA` solo mira rutas seguras de hasta tres pasillos y penaliza cruzar una power pill, así que si las últimas píldoras están detrás de una power pill o de un pasillo vigilado, no llega. Habría que tocar la navegación. **Se descarta.**

---

## lun 16 nov · Elección para el concurso

**Va `mspacman-v5.fcl`.** Mismo resultado de media que la V3 de la P2, la HFSM que era nuestra mejor: 5672 frente a 5717 en la misma tanda, −45 con un umbral de 149, ruido. Pero es **mejor contra los dos rivales más difíciles** (Avanzado +202 y los fantasmas de Héctor +458, los dos por encima del umbral) y peor solo contra Intermedio (−598). Además, todas sus reglas se activan (16 de 16, sumando 3 partidas contra cada rival) y no tiene ningún caso MAL en ProbarBorroso. La v6 hace lo mismo con dos reglas, una entrada y una acción más.

Tabla de la tanda final (`P2V3,v5,v6`, 300 partidas por rival):

| Versión | Puntos (media de 6) | Nivel | Turnos por vida | Fantasmas comidos |
|---|---|---|---|---|
| P2 V3 (HFSM) | 5717 ± 56 | 0,205 | 2917 | 7,84 |
| **v5 (borrosa)** | **5672 ± 50** | 0,204 | 2921 | 7,95 |
| v6 | 5644 ± 49 | 0,186 | 2976 | 7,98 |

**Siguiente, si hubiera tiempo:**

1. Cerrar el hueco de la curva de `espera` hacia los 80 ticks.
2. Que `irA` y `escapar` puedan mirar más allá de tres pasillos. Es lo que deja a Ms. Pac-Man atascada al final del nivel y lo que hace que la atrapen Avanzado y Héctor.

La Ms. Pac-Man de la Práctica 3 no está entre los rivales: no la teníamos al fijar los rivales, y añadirla ahora obligaría a repetir todas las medidas.

**Antes de subir.** La pasada de Comparar con la versión elegida (tanda final, 5400 partidas) terminó sin ningún OJO ni Exception ni *Error evaluating fuzzy logic*. No hay TrazaBorrosa enganchada ni estado en atributos `static` en el controlador. El `FuzzyEngine` se crea en el constructor. El preproceso de los cuatro laberintos también se hace en el constructor; sin él, el primer turno tardaba unos 470 ms.

---

## IA

Esta práctica se ha hecho con ayuda de Claude (Anthropic). El prompt, tal cual:

> Con el statement de la práctica 4 y los temas de razonamiento borroso hay que hacer un controlador de MsPacman que supere a los demás controladores que hemos hecho con estas nuevas técnicas: [enlaces a la Práctica 4 y a los apuntes del tema 4]

Qué hizo:

- Portó la percepción y las acciones de la V3 de la P2.
- Escribió las versiones v0–v6, `ProbarBorroso` y las herramientas de medida.
- Ejecutó todas las tandas de Comparar.
- Redactó este diario a partir de los resultados.

**Qué cambiamos o revisamos nosotros:** *[PENDIENTE — una línea del grupo.]*


# Parte 2 · Implementación de los Fantasmas

**Rivales:** las tres Ms. Pac-Man de referencia (`Basico`, `Intermedio`, `Avanzado`), fijadas desde el principio. 300 partidas por versión y rival, semillas 1–300, límite de 4000 turnos (`COMPARO_FANTASMAS = true` en `Comparar`).
**Métricas:** puntos concedidos, turnos por vida, fantasmas comidos. **En las tres métricas, menos es mejor** para los fantasmas.

**Cómo está hecho.** Cada fantasma tiene su propio `FuzzyEngine` (bloque `Ghost`) creado en el constructor de `Ghosts`, y todos cargan el mismo fichero `.fcl` evaluado desde el punto de vista de ese fantasma (`yo`).

- **Percepción y utilidades (`GhostsInput`, `Movimientos`, `GhostsConstants`):** reutilizamos el cálculo de tramos/caminos sin vuelta atrás de nuestra Práctica 3, adaptando las distancias y márgenes a rangos numéricos acotados y traduciendo las comparaciones entre compañeros de Jess a variables numéricas relativas. Además, un observador guarda el identificador de la última acción elegida en el turno anterior para permitir histéresis (`estabaBloqueando`).

| Entrada | Qué mide | Rango |
|---|---|---|
| `pacman` | distancia de `yo` a Ms. Pac-Man (150 si está en la cárcel) | 0..150 |
| `distPeligro` | menor distancia entre Ms. Pac-Man $\rightarrow$ `yo` y `yo` $\rightarrow$ Ms. Pac-Man | 0..150 |
| `ppill` | distancia de Ms. Pac-Man a su power pill activa más cercana | 0..150 |
| `distPP` | distancia de `yo` a esa misma power pill | 0..150 |
| `comestible` | turnos restantes de comestible (0 si no lo es) | 0..200 |
| `margenBloqueo` | ventaja en turnos para llegar antes que ella a la PP amenazada | −50..50 |
| `soyMejorBloqueo` | 1 si ningún otro fantasma activo tiene mayor `margenBloqueo`, 0 si no | 0..1 |
| `margenCorte` | ventaja en turnos para interceptar el camino previsto de Ms. Pac-Man | −50..50 |
| `soyMejorCorte` | 1 si es el mejor interceptor (descartando al perseguidor más cercano), 0 si no | 0..1 |
| `margenCierre` | ventaja para cerrar la salida del pasillo si ella está atrapada sin PP | −50..50 |
| `soyMejorCierre` | 1 si tiene la mejor ventaja para cerrar el pasillo, 0 si no | 0..1 |
| `puestoCercania` | ranking de cercanía a Ms. Pac-Man entre activos no comestibles | 1..4 |
| `tieneRefugio` | 1 si existe un compañero no comestible al que llega antes que ella, 0 si no | 0..1 |
| `estabaBloqueando` | 1 si en el turno anterior ganó `bloquearPP`, 0 si no | 0..1 |

- **Acciones (`ghosts/actions/`):** portadas de la Práctica 3 implementando `Action` (sin `parseFact` y con `toString()` devolviendo `getActionId()`). `RefugiarseAction` calcula internamente el compañero más seguro en `execute(Game)` en lugar de recibirlo por parámetro.
  - *Supervivencia (comestible):* `huidaEficiente` (mira dos cruces por delante), `refugiarse` (hacia compañero activo), `dispersarse` (reparto voraz de esquinas), `huir` (básica del ejemplo).
  - *Peligro por píldora de poder:* `bloquearPP` (adelantarse a la PP amenazada y salir al encuentro), `huirDePP` (apartarse anticipando la inversión de sentido del motor al comerse la PP).
  - *Caza coordinada:* `cerrarPaso` (cerrar cruce de salida de pasillo sin PP), `interceptar` (cortar camino por delante), `cazar` (flanquear evitando caminos de compañeros), `vigilarPP` (cubrir PP cercana a ella), `perseguir` (básica del ejemplo).

Todas las salidas se evalúan en la escala `0..10` (`poco`, `algo`, `mucho`, `METHOD : COG`, `DEFAULT := 0`) y `MaxActionSelector` desempata siguiendo el orden del array (supervivencia $\rightarrow$ peligro PP $\rightarrow$ caza).

---

## sab 14 nov · v0 y v1: línea base del ejemplo (2 y 3 reglas)

**Qué hacen.**
- `ghosts-v0.fcl` (2 reglas): si `comestible IS si` $\rightarrow$ `huir IS mucho`; si `comestible IS no` $\rightarrow$ `perseguir IS mucho`.
- `ghosts-v1.fcl` (3 reglas): añade `R3` (`IF comestible IS no AND pacman IS cerca AND ppill IS cerca THEN huir IS mucho, perseguir IS poco`) para que los fantasmas cercanos se aparten cuando Ms. Pac-Man amenaza una power pill. Se declaran las 14 entradas en ambos `.fcl` para evitar *No such variable* con el nuevo `GhostsInput` (portado y modificado desde la practica 3).

**ProbarBorrosoGhosts (sobre `v1`).**
- 4/4 casos base en `BIEN`, sin empates: comestible $\rightarrow$ `huir` (8,66 vs 0,00); no comestible con PP lejos $\rightarrow$ `perseguir` (8,66 vs 0,00); cerca de ella y PP amenazada $\rightarrow$ `huir` (8,66 vs 4,99, activando `R2` y `R3` a 1,00); lejos de ella con PP amenazada $\rightarrow$ `perseguir` (8,66 vs 0,00).
- Curva de control de `ppill` (0 a 70 con `pacman = 15`, `comestible = 0`): `huir` gana hasta `ppill = 30` (bajando de 8,66 a 8,24 mientras `perseguir` sube de 4,99 a 6,56), y en `ppill = 35` (fin de `cerca`) cambia limpiamente a `perseguir` (8,66).

**Comparar (`v1` frente a `v0`, menos es mejor).**
```
MEDIA (3)             5919 ± 94       6114 ± 106      +195      284  ruido
v1 es mejor contra 2 rivales, peor contra 1 y no se distingue contra 0.
```

---

## sab 14 nov · v2: v1 + reglas de tecnicas defensivas (9 reglas)
**Qué cambia.** 
Incorporamos todo el bloque de comportamientos defensivos: `huidaEficiente`, `refugiarse` y `dispersarse` para cuando el fantasma es comestible, y `bloquearPP` y `huirDePP` cuando Ms. Pac-Man amenaza una píldora de poder. 
Como tanto la percepción como las 11 acciones ya están portadas y registradas en Java, el cambio consiste únicamente en añadir el nuevo fichero `ghosts-v2.fcl`.

**ProbarBorroso.** Dos casos nuevos: «fantasma a 10, margen 25 → recolecta» y «margen 10 → evade»; los 16 dan BIEN. La curva de `margen` con el fantasma a 10 ticks cruza suave: escapa hasta 5 de margen, evade de 5 a 14, y a partir de 17 recolecta (`evadir` baja de 8,6 a 5,0 y `recolectar` sube de 1,4 a 8,6). No hay huecos.
**Comparar (v2 frente a v1).**

```
MEDIA (3)            6049 ± 101        4812 ± 71     -1237      247  mejor
v2 es mejor contra 3 rivales, peor contra 0 y no se distingue contra 0.
```  
**Valoración.** El bloque defensivo supone una mejora sustancial tanto en puntos concedidos como en supervivencia frente a las versiones anteriores, reduciendo mucho los fantasmas comidos; se queda como nueva referencia, aunque todavía queda por portar el bloque de caza coordinada que implementamos en la P3 (`cerrarPaso`, `interceptar`, `cazar` y `vigilarPP`) para mejorar la fase ofensiva en la siguiente versión.

---

## sab 14 nov · v3: v2 + caza coordinada completa de la P3 (15 reglas)

**Qué cambia.** Completamos el traslado de la lógica de la Práctica 3 añadiendo las 4 acciones ofensivas que faltaban mediante seis reglas nuevas (`R10`–`R15`) en `ghosts-v3.fcl`:
- **R10–R11 (`cerrarPaso`):** si Ms. Pac-Man se mete en un pasillo sin salida segura (`margenCierre IS positivo`) y somos el fantasma mejor situado (`soyMejorCierre IS si`), tapamos el cruce de escape y bajamos `perseguir` y `cazar`.
- **R12–R13 (`interceptar`):** si no somos el perseguidor más cercano (`puestoCercania IS otro`) y llegamos con ventaja a cortar su camino (`margenCorte IS positivo` y `soyMejorCorte IS si`), interceptamos por delante frenando `perseguir` y `cazar`.
- **R14 (`vigilarPP`):** cuando Ms. Pac-Man ronda una *power pill* a media distancia (`ppill IS media`) y estamos cerca de ella (`distPP IS cerca`), la cubrimos antes de que se acerque del todo.
- **R15 (`cazar`):** si somos perseguidor secundario (`puestoCercania IS otro`) a media distancia (`pacman IS media`), flanqueamos por caminos distintos en lugar de ir todos en fila india detrás del líder.

**ProbarBorrosoGhosts.**
- Añadimos cuatro casos nuevos para `cerrarPaso`, `interceptar`, `vigilarPP` y `cazar`. Los 11 casos salen `BIEN` y sin empates (`8,66` frente a `4,99` o `0,00`).
- En el caso de intercepción (`puestoCercania = 2` y `pacman = 45`), saltan a la vez `R12`–`R13` y `R15` (flanqueo), pero como `R13` baja `cazar IS poco`, gana limpiamente `interceptar` (`8,66` frente a `4,99`).
- La curva de `ppill` (de 0 a 70 con `distPP = 15`) encadena las tres acciones sin huecos: gana `huirDePP` hasta `ppill = 25` (`8,66` a `8,44`), pasa a `vigilarPP` entre `30` y `65` (con pico de `8,66` en `45`) y cambia a `perseguir` (`8,66`) a partir de `70`.

**Traza** (`VerPartida` con `TrazaBorrosa` en `BLINKY` contra `MsPacManAvanzado`, 156 turnos, 6980 puntos):
- Gana `perseguir` en 62 turnos, `cazar` en 49, `interceptar` en 16, `dispersarse` en 14, `huidaEficiente` en 7, `cerrarPaso` en 5 y `huirDePP` en 3.
- Las reglas nuevas de ataque (`R10`–`R15`) entran todas en juego con normalidad. En cambio, detectamos **reglas muertas (`0` activaciones)** en el bloque defensivo heredado de la v2: **`R3` (`refugiarse`) y `R5`–`R8` (`bloquearPP`) no se activan ni una sola vez** en toda la partida (`tieneRefugio` estuvo siempre a `0` y, cuando Ms. Pac-Man fue a por una *power pill*, `BLINKY` llegaba tarde con `margenBloqueo < 0`, disparando `R9` para apartarse).

**Comparar (`v3` frente a `v2`, 300 partidas por rival, semillas 1–300, menos es mejor).**
```
MEDIA (3)             4862 ± 75        3280 ± 52     -1582      182  mejor
v3 es mejor contra 2 rivales, peor contra 1 y no se distingue contra 0.
``` 
**Valoración.** La caza coordinada supone un salto enorme y nos acerca mucho al nivel de la P3: recorta `1582` puntos en la media concedida (sobre todo frente a `MsPacManAvanzado`) y baja los fantasmas comidos a `3,58`. **Se queda** como versión de referencia, pero queda pendiente en las siguientes iteraciones revisar esas **reglas muertas** (`R3` y `R5`–`R8`) para ajustar sus condiciones.

---

## lun 16 nov · v4: limpieza de reglas muertas y freno al flanqueo (10 reglas) → descartada

**Problema.** En la v3 teníamos 5 reglas muertas (`refugiarse` y `bloquearPP`) que no ganaban ni un turno. Además, contra `Intermedio` los secundarios se lanzaban a flanquear (`cazar`) cuando Ms. Pac-Man estaba a punto de coger una *power pill*, y el líder soltaba persecuciones encima de ella para vigilar píldoras (`t=24`).

**Qué cambia.** Quitamos los bloques muertos dejando `ghosts-v4.fcl` en 10 reglas, limitamos `vigilarPP` (`R9`) a fantasmas secundarios y exigimos `ppill IS lejos` para flanquear con `cazar` (`R10`).

**ProbarBorrosoGhosts.**
- Los 11 casos salen `BIEN` y sin empates (`8,66` frente a `4,99` o `0,00`), resolviendo los fallos de las versiones anteriores
- La curva de `ppill` (de 0 a 70 con `distPP = 15`) encadena bien las tres acciones: `huirDePP` hasta `30` (`8,66` a `8,24`), `vigilarPP` entre `35` y `65` (pico de `8,66` en `50`) y `perseguir` (`8,66`) en `70`, sin huecos.

**Comparar (`v4` frente a `v3`, 300 partidas por rival, semillas 1–300, menos es mejor).**
```
MEDIA (3)             3412 ± 55        3526 ± 57      +114      159  ruido
v4 es mejor contra 0 rivales, peor contra 1 y no se distingue contra 2.
```

**Valoración.** Aunque en las pruebas unitarias iba fina, en partida real empeora: la media de puntos se queda en ruido (`+114`), suben los fantasmas comidos (`+0,28`, peor) y contra `Basico` regala `+229` puntos. Al exigir `ppill IS lejos` para flanquear, los fantasmas tardan demasiado en rodear y dejan escapar a rivales sencillos. **Se descarta** (volvemos a la `v3` como base para la `v5`, donde limpiaremos solo las reglas muertas sin frenar tanto el ataque).

---

