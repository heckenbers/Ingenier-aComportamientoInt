# Diario · Práctica 4 · grupoHLI_CSB_EJB

**Integrantes:** Elkin Joel Benitez Castillo · Carlos Salvador Buhigas de las Heras · Héctor López Ibáñez

**Concurso:**
 - **MsPacMan:** `mspacman-v14.fcl` (18 reglas), la que carga `MsPacMan()`. Ver la Parte 1.
 - **Ghosts:** `ghosts-v6.fcl` (12 reglas), la que carga `Ghosts()`. Ver la Parte 2.

# Parte 1 · Implementación de Ms. Pac-Man

**Rivales:** los cinco fantasmas de referencia (Random, Aggressive, Basico, Intermedio, Avanzado) más los fantasmas de Héctor de la Práctica 2 (*P2 Hector*), fijados desde el principio. Desde el 17 de noviembre, también los fantasmas de la Práctica 3 (*P3 fantasmas*); las tandas de 6 y de 7 rivales no se mezclan. 300 partidas por rival, semillas 1–300, límite de 4000 turnos.
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

## mar 17 nov · La Práctica 3 entra en la vara

**Rivales nuevos.** Desde hoy, todas las tandas tienen un **séptimo rival fijo**: los fantasmas de la Práctica 3 (*P3 fantasmas*, reglas Jess, `ghosts-v4.clp`). También se puede elegir como versión la **Ms. Pac-Man de la P3** (*P3 reglas*, `mspacman-v1.clp`, la de su concurso). Las dos se compilan aparte con sus `.clp` copiados, que Jess lee desde el classpath. Las tandas de ayer, con 6 rivales, no se mezclan con las de hoy, con 7.

---

## mar 17 nov · v7: la regla 5 solo sin cebo (16 reglas)

**Problema.** Es el hueco que dejamos apuntado ayer. Con un cebo de dos y unos 80 ticks de espera, ganaba `recolectar` (5,8) entre esperar y comer (5,0 y 5,0). La causa es la regla 5 («nadie cerca: recolectar mucho»): se activaba a tope mientras las reglas del cebo solo se activaban a medias (0,43), y con MAX como acumulación `recolectar` se quedaba con su `mucho`.

**Cambio.** R5 exige además `cebo IS nada`. Con cebo, `recolectar` lo deciden las reglas del cebo.

**ProbarBorroso.** El caso nuevo era *«cebo de 2 tras 80 ticks: no recolecta»*. Salió MAL con EMPATE, pero no por `recolectar`, que ya bajaba a 1,6. El EMPATE era entre `esperarCebo` y `comerCebo`, que valían 5,0 los dos: 80 es justo el punto de cruce entre esperar y comer, y el caso estaba mal planteado. Lo cambié por dos casos, uno a cada lado: a los 70 ticks espera y a los 90 se lo come. Los 18 dan BIEN.

Curva de `espera` (cebo de 2) en la v5 y en la v7:

```
espera   v5                                         v7
  70     esperar 7,1  comer 2,9  recolectar 5,2     esperar 7,1  comer 2,9  recolectar 1,4
  80     esperar 5,0  comer 5,0  recolectar 5,8 ←   esperar 5,0  comer 5,0  recolectar 1,6
  90     esperar 2,9  comer 7,1  recolectar 5,2     esperar 2,9  comer 7,1  recolectar 1,4
```

---

## mar 17 nov · v8: + hambre y rumbo (19 reglas, una entrada y una acción nuevas)

**Problema.** La v6 no arregló los atascos del final del nivel, y ayer atribuimos la causa a la búsqueda de rutas. Hoy lo miramos con una herramienta nueva, `Atasco`: juega hasta que pasan 300 turnos sin comer ninguna píldora y describe la situación. Contra Random:

- **No es la búsqueda de rutas.** Con todos los fantasmas a más de 40 pasos, ninguna power pill en el camino y la píldora a 5–20 pasos, `recolectar` elegía el movimiento **contrario** al del camino más corto. Lo más probable es la penalización de 200 por meterse en un callejón, que se aplica aunque no haya ningún peligro.
- **El cebo contra Random.** Esperaba junto a la power pill a unos fantasmas que no venían, y `espera` volvía a 0 cada vez que `comerCebo` ganaba un turno, así que la espera se alargaba.
- **Ninguna entrada decía cuánto tiempo llevaba sin comer**, que es justo el síntoma.

**Cambio.**

- **Entrada nueva `hambre`:** ticks sin comerse ninguna píldora, de 0 a 300. La lleva `MemoriaPacMan`, que se actualiza al principio de cada tick. Se declara también en los .fcl de la v0 a la v7, sin reglas.
- **Acción nueva `rumbo`:** el primer paso del camino más corto a la píldora más cercana, o a la power pill más cercana si ya no quedan normales. No puntúa nada; solo comprueba que la ruta que empieza por ese movimiento no sea insegura.
- **R21** (con hambre, sin peligro, sin nada que cazar y sin un cebo excelente): `rumbo` mucho, `recolectar` poco. **R22:** con lo mismo, deja de esperar el cebo. **R23:** sin hambre, `rumbo` poco.

**Un fallo de la acción, encontrado antes de medir.** En la primera prueba con `Atasco`, `rumbo` pedía el movimiento del camino más corto y, aun así, la distancia a la píldora **subía**. El motor lo explica: **Ms. Pac-Man no puede dar media vuelta**. `Game.correctPacManDir` ignora la dirección opuesta a la que lleva y sigue recto. La P3 ya lo había visto, porque su diario dice que el movimiento hacia atrás en un pasillo no es legal para Ms. Pac-Man. Las rutas de la V3 ya lo tienen en cuenta (`getPossibleMoves(nodo, últimoMovimiento)`), pero mi `rumbo` usaba el camino más corto normal. Ahora usa la distancia y el siguiente movimiento **sin poder invertir** (`getShortestPathDistance` y `getNextMoveTowardsTarget` con el último movimiento). Con eso, en la traza turno a turno la distancia baja de 28 a 1 y se come la píldora.

**ProbarBorroso.** Seis casos nuevos:

- Con 150 ticks sin comer: si no pasa nada, `rumbo`; con un fantasma a 10, `evadir`; con dos a tiro, `cazar`; con un cebo de 3, `comerCebo`; esperando un cebo de 2, `rumbo`.
- Con solo 40 ticks sin comer, `recolectar`.

Los 24 dan BIEN. La curva de `hambre` pasa de `recolectar` a `rumbo` hacia los 95 ticks, sin huecos.

---

## mar 17 nov · Comparar: P2 (HFSM), P3 (reglas) y v5 (borrosa), 7 rivales

Tanda `P2V3,P3,v5`: 300 partidas por rival, semillas 1–300, con los fantasmas de la P3 como séptimo rival. Sin ningún OJO ni Exception.

```
== puntos: P3 reglas frente a P2 V3 ==
MEDIA (7)   5563 ± 50  →  5454 ± 45   -109   2·ET 134  ruido   (peor solo contra Intermedio, -469)
== puntos: v5 frente a P3 reglas ==
rival            P3 reglas        v5          D   2·ET_D
Random          4980 ± 95    4969 ± 93      -10      265  ruido
Aggressive      7919 ± 184   7980 ± 192     +61      533  ruido
Basico          6697 ± 143   6724 ± 138     +27      398  ruido
Intermedio      6225 ± 139   6052 ± 128    -173      379  ruido
Avanzado        4155 ± 67    4144 ± 65      -11      187  ruido
P2 Hector       3752 ± 66    4086 ± 74     +334      198  mejor
P3 fantasmas    4453 ± 89    4658 ± 99     +205      266  ruido
MEDIA (7)       5454 ± 45    5516 ± 45      +62      128  ruido
v5 es mejor contra 1 rivales, peor contra 0 y no se distingue contra 6.
```

En nivel, turnos por vida y fantasmas comidos, la v5 frente a la P3 da ruido en la media.

**Lectura.** Las tres Ms. Pac-Man están igualadas. No sorprende: las tres usan la percepción y las acciones de la V3 de Carlos, y lo único que cambia es quién decide (una HFSM, reglas Jess o un FCL). La borrosa no es peor que ninguna y es la única que se distingue contra un rival, los fantasmas de Héctor, que son los que más castigan. Para superar a las otras de verdad hace falta cambiar algo que ninguna tiene, y por eso la v7 y la v8.

**Comparar (v7 frente a v5, y v8 frente a v7; 7 rivales, sin ningún OJO).**

```
== puntos: v7 frente a v5 ==
MEDIA (7)   5594 ± 48  →  5467 ± 46   -127   2·ET 132  ruido   (peor contra Random, -300)
            nivel ruido · turnos por vida ruido · fantasmas comidos 7.89 → 7.71 peor
== puntos: v8 frente a v7 ==
rival            v7            v8           D   2·ET_D
Random          4939 ± 90    4864 ± 85      -75      248  ruido
Aggressive      7911 ± 196   7344 ± 144    -567      486  peor
Basico          6857 ± 142   6072 ± 128    -785      382  peor
Intermedio      5781 ± 126   5597 ± 105    -184      329  ruido
Avanzado        4101 ± 69    4074 ± 58      -27      181  ruido
P2 Hector       3981 ± 72    3957 ± 64      -24      193  ruido
P3 fantasmas    4698 ± 104   4473 ± 86     -225      270  ruido
MEDIA (7)       5467 ± 46    5197 ± 38     -270      119  peor
            nivel 0.195 → 0.122 peor · turnos por vida 2702 → 2776 mejor · fantasmas comidos 7.71 → 7.44 peor
```

**v7: se descarta.** Cierra el hueco de la curva, pero en las partidas no mejora nada: es ruido de media, peor contra Random y come algo menos. Con MAX, que `recolectar` ganara justo en el cruce de los 80 ticks hacía que Ms. Pac-Man comiera algo mientras esperaba, y eso no era tan malo como parecía en la curva. Una curva más limpia no es por sí sola un mejor juego.

**v8: se descarta.** Vive más (+74 turnos por vida), pero acaba muchos menos laberintos (nivel 0,195 → 0,122) y come menos fantasmas. Pierde sobre todo contra Aggressive y Basico, que son contra los que el cebo da más.

Nuestra hipótesis, sin comprobar: R22 deja de esperar el cebo cuando lleva mucho sin comer. Patrullando junto al cebo, Ms. Pac-Man casi no come, así que `hambre` sube durante la espera y la regla la saca del cebo antes de que lleguen los fantasmas. Contra Random los atascos se reducían en las pruebas con `Atasco`, pero eso se paga caro contra los que sí vienen.

Lo siguiente sería otra versión: v7 + `rumbo` sin la R22, y `hambre` contada solo cuando no se está esperando un cebo. La medimos más adelante.

---

## mar 17 nov · Elección (sin cambios)

**Sigue a concurso `mspacman-v5.fcl`.** En las dos tandas de hoy, con 7 rivales:

| Versión | Puntos (media de 7) | Contra quién se distingue |
|---|---|---|
| P2 V3 (HFSM) | 5563 ± 50 | — |
| P3 (reglas Jess) | 5454 ± 45 | ruido frente a la V3; peor contra Intermedio |
| **v5 (borrosa)** | **5516 ± 45** (5594 en la otra tanda) | ruido frente a la P3; **mejor contra los fantasmas de Héctor (+334)** |
| v7 | 5467 ± 46 | ruido frente a la v5 |
| v8 | 5197 ± 38 | peor frente a la v7 |

La borrosa iguala a la HFSM y a las reglas, y es la única que se distingue para bien contra un rival. Las dos mejoras de hoy no la superan, así que no se cambia.

---

## mié 18 nov · v9: rumbo cuando no pasa nada (sobre la v5) → descartada

**Problema.** En el perfil, `recolectar` comía solo 7–8 píldoras cada 100 turnos aunque no hubiera ningún fantasma cerca.

**Cambio.** Cuando no pasa nada (nadie cerca, sin cebo, nada que cazar), `rumbo`: el camino más corto a la píldora más cercana, respetando que Ms. Pac-Man no puede dar la vuelta, en vez de `recolectar`.

**Resultado.** Peor que la v5 en la prueba rápida. Ir a por la píldora más cercana sin mirar nada más la mete en zonas malas. No guardamos la tabla de esa prueba. **Se descarta.**

---

## mié 18 nov · v10 a v13: tocar umbrales (sobre la v5) → descartadas

Cuatro versiones que solo cambian dónde cruzan los conjuntos, sin reglas nuevas:

- **v10:** paciencia corta con el cebo; pasa de esperar a comer hacia los 40 ticks (antes, unos 75).
- **v11:** paciencia larga; el cruce pasa a los 110 ticks.
- **v12:** huir más tarde: `cerca` va de 4 a 15 ticks y `lejos` empieza en 11 (antes, de 5 a 18 y en 13).
- **v13:** huir antes: `cerca` llega a 22 ticks y `lejos` empieza en 16.

**Criba (40 partidas por rival, las mismas para todas).**

```
          Basico       Aggressive   Avanzado     Hector       P3           MEDIA
v5        6012 ± 461   8599 ± 656   4661 ± 361   4031 ± 288   4242 ± 264   5509 ± 192
v10       6085 ± 500   7005 ± 620   4124 ± 377   4082 ± 217   4332 ± 258   5125 ± 189
v11       6987 ± 654   7481 ± 714   4133 ± 208   3938 ± 247   4957 ± 508   5499 ± 228
v12       7584 ± 681   6975 ± 466   4237 ± 234   3947 ± 237   4208 ± 208   5390 ± 183
v13       6677 ± 555   8485 ± 637   3921 ± 231   4419 ± 372   4104 ± 365   5521 ± 204
```

La v10 es peor (−384). La v11, la v12 y la v13 están dentro del ruido. Mover umbrales no da nada; lo que falla es lo que puede hacer cuando la acorralan. **Se descartan las cuatro.**

---

## mié 18 nov · v14: territorio (sobre la v5, 18 reglas)

**Problema.** Cuando no hay ruta segura, `escapar` solo mira tres pasillos y a veces se mete en una trampa.

**Cambio.** Una acción nueva, `territorio`. Con un BFS desde Ms. Pac-Man y desde cada fantasma, cuenta los nodos a los que llega ella antes que ninguno, y sale por el movimiento que deja la zona propia más grande. Mira todo el laberinto. **R26 y R27:** sin ruta segura (margen nulo) y sin el fantasma encima, `territorio`.

**Criba.**

```
          Basico       Aggressive   Avanzado     Hector       P3           MEDIA
v5        6668 ± 393   7991 ± 472   4189 ± 171   4316 ± 172   4369 ± 258   5507 ± 142
v14       6748 ± 406   7670 ± 525   4284 ± 158   4290 ± 204   5247 ± 282   5648 ± 153
```

Pasa a Comparar.

**Comparar (v14 frente a v5, 7 rivales, 300 partidas por rival).**

```
== puntos: v14 frente a v5 ==
rival                v5           v14          D   2·ET_D
Random          4979 ± 97    4962 ± 90     -17     264  ruido
Aggressive      7963 ± 193   8275 ± 210   +312     571  ruido
Basico          6599 ± 143   7120 ± 137   +521     395  mejor
Intermedio      6043 ± 127   6211 ± 137   +168     373  ruido
Avanzado        4302 ± 69    4201 ± 63    -101     187  ruido
P2 Hector       3917 ± 64    4426 ± 83    +509     210  mejor
P3 fantasmas    4702 ± 100   5105 ± 117   +403     309  mejor
MEDIA (7)       5501 ± 46    5757 ± 48    +257     133  mejor
v14 es mejor contra 3 rivales, peor contra 0 y no se distingue contra 4.
```

**Es la primera versión que sale «mejor» de verdad.** El nivel también sale mejor (0,175 → 0,241).

---

## mié 18 nov · v15, v16 y v17: más territorio (sobre la v14) → descartadas

- **v15:** `territorio` también con el fantasma cerca y el margen justo (antes, `evadir`). Tiene un fallo: el cambio en la R27 deja sin efecto la R26. Es el MAL que da ProbarBorroso en la v15; no lo arreglamos porque la versión ya no iba a quedarse.
- **v16:** `territorio` también con el fantasma encima (antes, `escapar`).
- **v17:** una entrada nueva, `espacio` (tamaño de la zona libre). Si la zona se está cerrando con un fantasma no lejos, `territorio` aunque la ruta inmediata aún tenga algo de margen.

**Criba (cada una contra la v14 en su propia tanda).**

```
          Basico       Aggressive   Avanzado     Hector       P3           MEDIA
v14       6772 ± 396   7622 ± 465   4259 ± 238   4136 ± 182   4774 ± 339   5512 ± 152
v15       6632 ± 374   7573 ± 524   4722 ± 248   4181 ± 167   4635 ± 203   5549 ± 148

v14       7127 ± 407   7948 ± 500   4754 ± 255   4512 ± 313   4787 ± 268   5826 ± 161
v16       6596 ± 401   8143 ± 482   4393 ± 196   4440 ± 221   5497 ± 274   5814 ± 149

v14       7589 ± 416   8257 ± 704   4429 ± 226   4016 ± 139   4839 ± 323   5826 ± 184
v17       6772 ± 432   7578 ± 476   4403 ± 189   4603 ± 257   4788 ± 310   5629 ± 156
```

Ninguna mejora a la v14 (+37, −12 y −197). Usar `territorio` en más casos no ayuda: cuando hay ruta segura, `evadir` y `escapar` ya lo hacen bien. **Se descartan.**

---

## mié 18 nov · Comparar: P2 (HFSM), P3 (reglas) y v14 (borrosa), 7 rivales

Misma tanda para las tres, 300 partidas por rival.

| Versión | Puntos (media de 7) | Turnos por vida | Fantasmas comidos |
|---|---|---|---|
| P2 V3 (HFSM) | 5539 ± 47 | — | 7,64 |
| P3 (reglas Jess) | 5543 ± 44 | 2719 | 7,74 |
| **v14 (borrosa)** | **5779 ± 48** | **3062** | **8,12** |

```
== puntos: v14 frente a P3 reglas ==
rival            P3 reglas    v14          D   2·ET_D
Random          5105 ± 98    4969 ± 97    -136     277  ruido
Aggressive      8185 ± 198   8315 ± 206   +130     571  ruido
Basico          6475 ± 129   7139 ± 145   +664     389  mejor
Intermedio      6539 ± 118   6407 ± 131   -132     353  ruido
Avanzado        4128 ± 67    4349 ± 69    +221     193  mejor
P2 Hector       3886 ± 62    4329 ± 80    +443     201  mejor
P3 fantasmas    4481 ± 91    4945 ± 105   +464     277  mejor
MEDIA (7)       5543 ± 44    5779 ± 48    +236     130  mejor
v14 es mejor contra 4 rivales, peor contra 0 y no se distingue contra 3.
```

Frente a la P2 V3, +240 (umbral de unos 134): también mejor. En turnos por vida, la v14 es mejor que la P3 contra los 7 rivales.

---

## mié 18 nov · Elección: va la v14

**Va `mspacman-v14.fcl`.** Es la primera de nuestras Ms. Pac-Man que supera en la misma tanda a la HFSM de la P2 y a las reglas de la P3, y no es peor contra ningún rival. Las 18 reglas se activan y ProbarBorroso da 17 de 17 BIEN.

---

## mié 18 nov (tarde) · v18 y v19: territorio con más información

Es lo siguiente después de la v14: darle a `territorio` más información. Las dos son otra acción, con otra salida del FCL, para no cambiar la `territorio` que usa la v14. `TerritorioAction` y `Territorio.tamano` ahora aceptan los pesos como parámetros, y `MsPacMan` registra:

- `territorioComida` (v18): power pill = 60 nodos, y cada píldora de la zona suma 3. La idea es huir comiendo.
- `territorioPoder` (v19): power pill = 150 nodos, y las píldoras no suman. La idea es huir hacia una salida que da la vuelta a la persecución.

Las reglas son las de la v14 (R26 y R27), con la salida nueva.

**ProbarBorroso.** 17 de 17 BIEN en las dos. Los casos de `territorio` valen de la v14 a la v17, y la v18 y la v19 tienen el suyo con su salida.

**Criba (40 partidas por rival, las mismas para las tres).**

```
          Basico       Aggressive    Avanzado     Hector       P3           MEDIA
v14       7134 ± 386   7964 ± 533    3917 ± 159   4667 ± 248   4892 ± 214   5715 ± 150
v18       7636 ± 406   7997 ± 572    4711 ± 244   4262 ± 218   4485 ± 242   5818 ± 162
v19       6576 ± 391   7198 ± 415    4493 ± 272   4328 ± 212   4576 ± 228   5434 ± 141
```

- **v19, se descarta.** −281: dar tanto peso a la power pill la lleva a huir hacia ella aunque la zona sea pequeña.
- **v18 pasa a Comparar.** Es +103 de media, que en la criba es ruido, pero mejora mucho contra Avanzado (+794).

**Comparar (v18 frente a v14, 7 rivales, 300 partidas por rival, sin ningún OJO).**

```
== puntos: v18 frente a v14 ==
rival            v14           v18          D   2·ET_D
Random          5030 ± 95    5177 ± 100    +147      276  ruido
Aggressive      7951 ± 201   8170 ± 209    +219      580  ruido
Basico          7112 ± 138   7464 ± 146    +352      402  ruido
Intermedio      6268 ± 136   6368 ± 123    +100      366  ruido
Avanzado        4224 ± 69    4261 ± 71      +37      199  ruido
P2 Hector       4344 ± 85    4336 ± 78       -8      232  ruido
P3 fantasmas    4961 ± 108   4985 ± 111     +24      309  ruido
MEDIA (7)       5699 ± 47    5823 ± 48     +124      135  ruido
v18 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 7.
   nivel 0.235 → 0.258 · turnos por vida 3003 → 3028 · fantasmas comidos 8.00 → 8.13   (todo ruido)
```

**v18: no se queda, pero no por mala.** Va en la buena dirección en las cuatro métricas y no es peor contra ningún rival, pero +124 no llega al umbral de 135. La regla que hemos seguido toda la práctica es que solo se queda lo que sale «mejor», y este resultado no lo demuestra. Con otra tanda de semillas (`-Dsemilla=1001`) se podría confirmar, pero no hay tiempo antes de la entrega. **Sigue a concurso la v14.** La v18 queda en el zip como la siguiente candidata.

---

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

## lun 16 nov · v5: bloqueo temprano de PP, flanqueo continuo y cierre ajustado (13 reglas)

**Problema.** Viendo partidas de la v3 contra `MsPacManAvanzado` detectamos cuatro fallos:
1. `bloquearPP` nunca saltaba porque exigía `ppill IS cerca`; en turnos en los que el fantasma tenía ventaja para taponar la píldora, pero esperaba demasiado y llegaba tarde.
2. Los fantasmas secundarios (`puestoCercania > 1`) solo flanqueaban con `cazar` a media distancia (`pacman IS media`), así que cuando venían desde lejos (`t=67`) iban todos juntos en fila india detrás del líder.
3. `cerrarPaso` e `interceptar` exigían margen positivo estricto (`> 0`), desaprovechando encerronas donde llegaban solo 1 o 2 pasos justos (`t=16`).
4. `refugiarse` estaba siempre a `0` (`tieneRefugio = 0` en toda la partida).

**Qué cambia.** Partimos de `v3`, quitamos `refugiarse` (regla muerta) y ajustamos `ghosts-v5.fcl` a **13 reglas**:
- Activamos `bloquearPP` (`R4`–`R6`, con histéresis) desde `ppill IS NOT lejos` para taponar y proteger las píldoras antes de que se acerque.
- Ampliamos `cazar` (`R13`) a `pacman IS NOT cerca` para que los secundarios flanqueen siempre que vengan de media o larga distancia y solo converjan a bocajarro.
- Adelantamos `margenCierre` (`-5..0`) y `margenCorte` (`-4..0`) para cerrar salidas y acorralar con más decisión.

**ProbarBorrosoGhosts.**
- Los 11 casos salen `BIEN` y sin empates (`8,66` frente a `4,99` o `0,00`, y `8,60` frente a `5,14` en los cierres y cortes con margen ajustado).
- En la curva de `ppill` (de 0 a 70 con `distPP = 15` y sin ventaja de bloqueo), gana `huirDePP` hasta `25` (`8,66` a `8,31`) y pasa sin huecos a `vigilarPP` desde `30` hasta `70` (pico de `8,66` en `50` y `8,19` en `70`, donde aún queda un resto de `media`).

**Traza** (`VerPartida` con `TrazaBorrosa` en `BLINKY` contra `MsPacManAvanzado`, 75 turnos, 4440 puntos):
- Gana `cazar` en 32 turnos, `perseguir` en 29, `dispersarse` en 4, `huidaEficiente` en 4, `huirDePP` en 3, `cerrarPaso` en 2 y `bloquearPP` en 1 (`t=3`, cuando ve ventaja temprana con `ppill = 82`).
- El flanqueo continuo funciona (los secundarios se abren desde lejos sin ir en fila india), pero siguen quedando reglas a `0` en la traza de `BLINKY`: la histéresis del bloqueo (`R6`, porque en `t=4` pierde el margen y suelta el bloqueo) y `interceptar` (`R10`–`R11`, al ir `BLINKY` casi siempre como líder `puestoCercania = 1`).

**Comparar (`v5` frente a `v4`, 300 partidas por rival, semillas 1–300, menos es mejor).**
```
MEDIA (3)             3612 ± 61        3282 ± 58      -331      168  mejor
v5 es mejor contra 2 rivales, peor contra 0 y no se distingue contra 1.
```

**Valoración.** Recuperamos todo lo perdido en la `v4` y conseguimos nuestros mejores números hasta ahora contra `Avanzado` (`5247` puntos y `7,01` fantasmas comidos), mejorando a la `v4` en las tres medias (`-331` puntos, `-89` turnos por vida y `-0,56` fantasmas comidos) sin empeorar contra ningún rival.

---

## mier 18 nov · v6: limpieza de regla muerta, suelta de vigilancia y cierre a 20 pasos (12 reglas)

**Problema.** En `v5` detectamos tres detalles de ajuste fino:
1. La regla de histéresis del bloqueo (`R6` de `estabaBloqueando`) quedó a `0` activaciones en toda la traza porque al perder ventaja en el turno siguiente el margen pasaba a negativo.
2. En la curva de `ppill`, `vigilarPP` seguía activo hasta `70` pasos por ser `media` demasiado ancho (`25..75`), reteniendo al fantasma vigilando píldoras ya lejanas.
3. Contra `MsPacManIntermedio` (`3127` puntos, empate en ruido), los secundarios flanqueaban hasta los `15` pasos, dejando algo de hueco en el remate final.

**Qué cambia.** Refinamos `ghosts-v6.fcl` dejando **12 reglas** limpias:
- Eliminamos la regla muerta de histéresis de `estabaBloqueando`.
- Recortamos `ppill IS media` a `trian 25 45 65` (y `lejos` desde `55`) para que `vigilarPP` suelte a partir de `65` pasos y vuelva a `perseguir`.
- Ampliamos `pacman IS cerca` a `(0, 1) (20, 1) (35, 0)` (y `media` a `trian 25 45 70`) para cerrar antes el cerco final.

**ProbarBorrosoGhosts.**
- Los 11 casos salen `BIEN` y sin empates (`8,66` frente a `4,99` o `0,00`, y `8,60` frente a `5,14` en cierres y cortes ajustados).
- En la curva de `ppill` (de 0 a 70 con `distPP = 15` y sin bloqueo), encadena las tres fases sin huecos: `huirDePP` hasta `25` (`8,66` a `8,31`), `vigilarPP` de `30` a `60` (pico de `8,66` en `45`) y vuelta limpia a `perseguir` (`8,66`) a partir de `65`.

**Traza** (`VerPartida` con `TrazaBorrosa` en `BLINKY` contra `MsPacManAvanzado`, 61 turnos, 2970 puntos):
- Gana `perseguir` en 31 turnos, `cazar` en 11, `huidaEficiente` en 7, `cerrarPaso` en 5, `bloquearPP` en 2 (`t=2`), `dispersarse` en 2, `vigilarPP` en 2 y `huirDePP` en 1.
- **Todas las reglas (`RULE 1` a `RULE 12`) se activan al menos 2 veces** en una sola partida, dejando el sistema completamente libre de reglas muertas.

**Comparar (`v6` frente a `v5`, 300 partidas por rival, semillas 1–300, menos es mejor).**
```
MEDIA (3)             3237 ± 53        3258 ± 59       +20      158  ruido
v6 es mejor contra 0 rivales, peor contra 0 y no se distingue contra 3.
```

---

**Valoración.** Empata estadísticamente con `v5` en todas las métricas (mejora ligeramente frente a `Intermedio` en `-139` puntos y baja la media de turnos por vida a `610`, todo dentro del ruido), pero corrige la curva de vigilancia y consigue que las 12 reglas se disparen en partida sin ninguna regla muerta. **Se queda** como versión definitiva.

---

## Elección para el concurso (Fantasmas)

Nuestra elección es **`ghosts-v6.fcl`** (12 reglas). Es la versión con mejor equilibrio global frente a los tres rivales de referencia (`3258` puntos concedidos de media, `610` turnos por vida y `3,54` fantasmas comidos), combinando bloqueo y huida ante *power pills* con caza coordinada (`cerrarPaso`, `interceptar` y flanqueo) y con el 100 % de sus reglas activas en partida.

---

## mié 18 nov · Juntar los fantasmas con la Ms. Pac-Man

Los fantasmas borrosos son de Héctor: `Ghosts.java`, el paquete `ghosts/` y los `.fcl` de la v0 a la v6. Se juntaron con la Ms. Pac-Man el 18 de noviembre, el mismo paquete `grupoHLI_CSB_EJB`, y solo se cambió el nombre (`getName` y `getTeam`) para que lleve el del grupo igual que la Ms. Pac-Man.

**Prueba al juntarlos** (5 partidas contra cada Ms. Pac-Man). Sin ningún error del motor borroso.

| Ms. Pac-Man rival | Puntos de media de Ms. Pac-Man |
|---|---|
| Basico | 1106 |
| Intermedio | 3946 |
| Avanzado | 4514 |
| la nuestra (v14) | 6502 |

Tiempo por turno: el primer turno tarda 255 ms (arranque); después, como mucho 17 ms.

## IA

Esta práctica se ha hecho con ayuda de Claude (Anthropic) y, en los fantasmas, también de Gemini. Los prompts de la Ms. Pac-Man, tal cual:

Versión v0:

«Necesitamos migrar la estructura base de nuestra HFSM V3 de la Práctica 2 a un sistema de reglas borrosas en FCL. Implementa las cuatro acciones principales (escapar, evadir, recolectar y cazar) utilizando las mismas entradas analíticas pero sin la gestión de cebo por el momento. Es un requisito estricto que todas las reglas definidas tengan activación real en partida y que el script de verificación ProbarBorroso valide correctamente todos los casos sin excepciones ni ambigüedades.»

Versión v1:

«Incorpora la lógica de gestión del cebo de la V3 sobre la base de la v0, añadiendo reglas específicas para los estados de espera y de ingesta de la píldora de fuerza. Una vez implementado, ejecuta un banco de pruebas comparativo de 300 partidas por rival frente a la v0 para cuantificar el impacto exacto en puntos y en fantasmas comidos.»

Versiones v2–v4:

«Queremos refinar el comportamiento defensivo: evalúa mediante dos variantes independientes si, al disponer de un margen holgado de ruta, la presencia cercana de un fantasma no debería forzar una huida prematura, y por otro lado, si debemos forzar explícitamente la acción de comer el cebo cuando el fantasma ya se encuentra a corta distancia. Aísla cada hipótesis en una versión independiente (v2, v3 y v4) para trazar de forma unívoca qué aporta cada modificación al rendimiento global.»

Versión v5:

«Realiza una auditoría de la base de reglas actual y elimina aquellas que no registren activaciones en las trazas o cuyo comportamiento sea redundante. Verifica mediante la herramienta de resumen que la compactación no altera la efectividad del controlador y que se mantiene la robustez frente a todos los rivales de referencia.»

Versiones v6, v9:

«Tras analizar las trazas de ejecución, hemos detectado que Ms. Pac-Man entra en bucles de inactividad o da vueltas improductivas al final de los niveles cuando quedan pocas píldoras dispersas. Diseña e implementa una nueva acción de navegación directa hacia la píldora más cercana que evite las penalizaciones de los callejones y resuelva el estancamiento sin comprometer la seguridad frente a los fantasmas.»

Versiones v7, v8:

«Modifica la regla de recolección (R5) para que permanezca inactiva cuando la estrategia de cebo esté activa, y experimenta añadiendo una nueva entrada al sistema que compute el "hambre" en función de los turnos consecutivos transcurridos sin ingerir puntos, evaluando si esto mejora la agresividad general del agente.»

Versiones v10–v13:

«Realiza un barrido paramétrico exhaustivo sobre los umbrales de paciencia en la espera del cebo y los rangos de distancia de amenaza de los fantasmas. Ejecuta una criba preliminar de 40 partidas por rival para cada combinación antes de seleccionar los mejores parámetros y lanzarlos a la tanda definitiva de 300 partidas.»

Versión v14:

«Abordemos el cuello de botella estructural de las rutas: cuando el sistema determine que no existe una ruta de huida segura bajo el criterio clásico de mirar tres pasillos, implementa un algoritmo BFS global que calcule con precisión geométrica qué sector del laberinto pertenece a Ms. Pac-Man (es decir, a qué nodos llega antes que cualquier fantasma), obligándola a maniobrar hacia la zona libre más amplia disponible.»

Versiones v15–v17:

«Amplía el ámbito de aplicación de la acción de territorio para que actúe también en escenarios críticos donde el fantasma se encuentra a distancia de contacto o inmediatamente encima, o bien indexada mediante una variable cuantitativa del volumen de la zona libre. Despliega cada variante en un entregable versionado independiente para medir su estabilidad.»

Versiones v18, v19:

«Optimiza la función de evaluación de la acción de territorio incorporando como factores ponderables el recuento de píldoras normales y de power pills presentes en el área accesible, parametrizando los pesos de forma externa sin alterar la lógica de decisión probada en la v14. Valida el comportamiento mediante cribas cruzadas antes de decidir su integración definitiva.»
Decisiones que tomamos nosotros durante el trabajo (Claude preguntaba y elegíamos):

- **Paquete:** `grupoHLI_CSB_EJB`, el mismo nombre que en la Práctica 2.
- **Alcance:** solo la Ms. Pac-Man borrosa. Los fantasmas los hacemos nosotros.
- **Rivales:** le subimos el zip de nuestra Práctica 3 para medir contra ella. Entraron sus fantasmas como séptimo rival fijo y su Ms. Pac-Man como vara.
- **El último día:** le pedimos dedicarlo entero a mejorar la Ms. Pac-Man, que es de donde salen la v9–v19.
- **Al final:** le pedimos probar alguna versión más (la v18 y la v19).

Qué hizo Claude:

- Portó la percepción y las acciones de la V3 de la P2.
- Escribió las versiones v0–v19, `ProbarBorroso` y las herramientas de medida (`Trazar`, `Muertes`, `Atasco`, `Perfil`, `Criba`).
- Ejecutó todas las tandas de Comparar.
- Redactó este diario a partir de los resultados.

**Qué hicimos nosotros:**
- Observación visual de las partidas y toma de decisiones: Ejecutamos `VerPartida` para buscar fallos tácticos.
- Ejecución y revisión de las pruebas, trazas y comparaciones: Con cada versión nueva revisamos la activación de reglas y los resultados, tomando nota de los cambios a hacer.
- Verificación final de la entrega de cada parte.
