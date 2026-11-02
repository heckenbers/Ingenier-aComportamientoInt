;;; =====================================================================
;;;  Ms. Pac-Man HLI_EJB_CSB  ·  versión 1
;;;  v0 (supervivencia, poder, recolección por defecto) + el CEBO de la HFSM
;;;  de la Práctica 2: esperar junto a una píldora de poder a que se acerquen
;;;  los fantasmas y comérsela cuando la cadena de capturas valga la pena.
;;;  Las 12 reglas de la v0 están sin cambios; lo nuevo va al final.
;;;
;;;  El Input (MsPacManInput) mide; estas reglas deciden; las acciones Java
;;;  calculan el movimiento. Gana el ACTION de mayor priority.
;;;
;;;  Capas:
;;;    salience 20  medir -> hechos intermedios desde MSPACMAN
;;;    salience 15  hechos intermedios que dependen de otros
;;;    salience 10  PODER (necesita saber ya si hay PELIGRO)
;;;    salience  5  CEBO_AGOTADO (necesita saber si hay PELIGRO o PODER)
;;;    salience  0  reglas de acción (leen los hechos intermedios)
;;;
;;;  PorRevertirTransition NO se traduce: medido en partidas, 'revertir' nunca
;;;  baja de 21 ticks mientras hay comestibles (un comestible tan cerca acaba
;;;  comido), así que con el umbral TAU_ALERTA = 12 la transición no saltaba
;;;  nunca tampoco en la Práctica 2. El slot revertir se queda en MSPACMAN.
;;;
;;;  Franjas de priority:
;;;    80-99 supervivencia · 60-79 poder · 10-59 recolectar
;;;    (dentro de recolectar: comer el cebo 45 > patrullar el cebo 40 > comer 10)
;;; =====================================================================

;;; ---------------------------------------------------------------------
;;;  Plantillas
;;; ---------------------------------------------------------------------

;; Lo único que asierta el Input. LEJOS (536870911) significa "no hay".
;; Los slots del cebo los usa la v1; se declaran aquí porque el Input es el
;; mismo para todas las versiones (sin ellos no se podría asertar el hecho).
(deftemplate MSPACMAN
   (slot amenaza)          ; ticks hasta que un fantasma no comestible llega a Pac-Man
   (slot margen)           ; ventaja (ticks) de la mejor ruta de escape (con lookahead)
   (slot revertir)         ; ticks hasta que un comestible vuelve a ser peligroso (sin uso)
   (slot comestibles)      ; fantasmas comestibles fuera de la cárcel
   (slot presa)            ; primer fantasma del plan de caza: BLINKY|PINKY|INKY|SUE|NONE
   (slot ultima)           ; id del ACTION ganador del turno anterior
   (slot ticksSinPeligro)  ; turnos seguidos huyendo sin PELIGRO
   (slot hayCebo)          ; true si hay una píldora de poder candidata a cebo
   (slot valorCebo)        ; puntos esperados si se come ahora esa píldora (valor_cadena)
   (slot poderRentable)    ; true si el tiempo de susto de este nivel da para cazar
   (slot pocasPildoras)    ; true si quedan pocas píldoras normales
   (slot ticksEnCebo)      ; turnos seguidos esperando en el cebo
   (slot ticksSinValorCebo); turnos seguidos en el cebo con valorCebo < VALOR_MIN_CEBO
   (slot ceboEnfriamiento) ; true durante ENFRIAMIENTO_CEBO turnos tras abandonar un cebo
   (slot aisladas (default 0)) ; píldoras del clúster aislado más cercano (0 si no hay); lo usa la v2
   (slot pildorasRestantes (default 1000))) ; píldoras normales que quedan; lo usa la v3

;; Hechos intermedios (sin slots): los asiertan unas reglas y los leen otras.
(deftemplate PELIGRO)
(deftemplate SIN_SALIDA)
(deftemplate CRITICO)
(deftemplate PODER)
(deftemplate CEBO)            ; v1: hay un cebo que merece la pena preparar
(deftemplate CEBO_AGOTADO)    ; v1: el cebo actual se abandona (MemoriaCebo enfría)

;; Salida. id = clave del mapa de acciones Java.
(deftemplate ACTION
   (slot id)
   (slot info (default ""))
   (slot priority)
   (slot fantasma (default NONE))    ; parámetro de CazarFantasma
   (slot modo (default patrullar)))  ; parámetro de PrepararCebo (v1): patrullar | comer

;;; ---------------------------------------------------------------------
;;;  Umbrales: los mismos valores que Parametros.java
;;; ---------------------------------------------------------------------
(defglobal ?*TAU_ALERTA*    = 12)   ; Parametros.TAU_ALERTA
(defglobal ?*TAU_CRIT*      = 6)    ; Parametros.TAU_CRIT
(defglobal ?*MARGEN_SEGURO* = 4)    ; Parametros.MARGEN_SEGURO = EAT_DISTANCE + 2
(defglobal ?*HISTERESIS*    = 3)    ; Parametros.HISTERESIS
(defglobal ?*VALOR_MIN_CEBO* = 600) ; Parametros.VALOR_MIN_CEBO (2 fantasmas: 200 + 400)
(defglobal ?*UMBRAL_CADENA*  = 1400); Parametros.UMBRAL_CADENA (3 fantasmas)
(defglobal ?*PACIENCIA_CEBO* = 80)  ; Parametros.PACIENCIA_CEBO
(defglobal ?*SIN_VALOR_MAX*  = 40)  ; Parametros.PACIENCIA_CEBO / 2 (CeboAgotadoTransition)

;;; ---------------------------------------------------------------------
;;;  Capa 1: hechos intermedios a partir de las medidas
;;; ---------------------------------------------------------------------

;; PeligroTransition, primera mitad: un fantasma llega pronto.
(defrule amenazaCercana
   (declare (salience 20))
   (MSPACMAN (amenaza ?a&:(<= ?a ?*TAU_ALERTA*)))
   =>
   (assert (PELIGRO)))

;; isSalidaSegura() de la Práctica 2, negado.
(defrule sinSalidaSegura
   (declare (salience 20))
   (MSPACMAN (margen ?m&:(<= ?m ?*MARGEN_SEGURO*)))
   =>
   (assert (SIN_SALIDA)))

;; CriticoTransition, primera mitad: un fantasma llega muy pronto.
(defrule amenazaCritica
   (declare (salience 20))
   (MSPACMAN (amenaza ?a&:(<= ?a ?*TAU_CRIT*)))
   =>
   (assert (CRITICO)))

;;; ---------------------------------------------------------------------
;;;  Capa 2: hechos intermedios encadenados
;;; ---------------------------------------------------------------------

;; PeligroTransition, segunda mitad: sin salida segura también es peligro.
(defrule sinSalidaEsPeligro
   (declare (salience 15))
   (SIN_SALIDA)
   =>
   (assert (PELIGRO)))

;; CriticoTransition, segunda mitad: sin salida segura también es crítico.
(defrule sinSalidaEsCritica
   (declare (salience 15))
   (SIN_SALIDA)
   =>
   (assert (CRITICO)))

;; PoderActivoTransition: hay comestibles y es seguro ir a por ellos.
;; Salience 10 < 15: se evalúa cuando PELIGRO ya existe (si toca).
(defrule poderActivo
   (declare (salience 10))
   (MSPACMAN (comestibles ?c&:(> ?c 0)))
   (not (PELIGRO))
   =>
   (assert (PODER)))

;;; ---------------------------------------------------------------------
;;;  Capa 3: acciones
;;; ---------------------------------------------------------------------

;; SUPERVIVENCIA / EscapeDeEmergencia (CriticoTransition)
(defrule escapeEmergencia
   (CRITICO)
   =>
   (assert (ACTION (id EscapeDeEmergencia)
                   (info "escapeEmergencia: fantasma muy cerca o sin salida segura")
                   (priority 95))))

;; SUPERVIVENCIA / Evadir (PeligroTransition, NoCriticoTransition)
(defrule evadir
   (PELIGRO)
   (not (CRITICO))
   =>
   (assert (ACTION (id Evadir)
                   (info "evadir: fantasma cerca, con salida")
                   (priority 85))))

;; SinPeligroTransition(HISTERESIS): no se sale de SUPERVIVENCIA hasta
;; llevar HISTERESIS turnos seguidos sin peligro.
(defrule evadirPorHisteresis
   (MSPACMAN (ultima Evadir|EscapeDeEmergencia)
             (ticksSinPeligro ?t&:(< ?t ?*HISTERESIS*)))
   (not (PODER))
   =>
   (assert (ACTION (id Evadir)
                   (info (str-cat "evadirPorHisteresis: " ?t " turnos sin peligro"))
                   (priority 84))))

;; PODER / CazarFantasma (PlanCazaDisponibleTransition). Acción con parámetro.
(defrule cazar
   (PODER)
   (MSPACMAN (presa ?g&~NONE))
   =>
   (assert (ACTION (id CazarFantasma) (fantasma ?g)
                   (info (str-cat "cazar: " ?g " es alcanzable"))
                   (priority 70))))

;; PODER / RecolectarEnPoder (PlanCazaVacioTransition)
(defrule recolectarEnPoder
   (PODER)
   (MSPACMAN (presa NONE))
   =>
   (assert (ACTION (id RecolectarEnPoder)
                   (info "recolectarEnPoder: ningún comestible alcanzable")
                   (priority 62))))

;; RECOLECTAR / IrAPildoraRentable: estado inicial, por defecto.
(defrule irAPildora
   (MSPACMAN)
   =>
   (assert (ACTION (id IrAPildoraRentable)
                   (info "irAPildora: por defecto")
                   (priority 10))))

;;; ---------------------------------------------------------------------
;;;  v1: el cebo (RECOLECTAR / PrepararCebo de la HFSM)
;;; ---------------------------------------------------------------------

;; CeboTransition, primera razón: la cadena de capturas vale la pena.
(defrule ceboPorValor
   (declare (salience 20))
   (MSPACMAN (hayCebo true) (poderRentable true) (ceboEnfriamiento false)
             (valorCebo ?v&:(>= ?v ?*VALOR_MIN_CEBO*)))
   =>
   (assert (CEBO)))

;; CeboTransition, segunda razón: quedan pocas píldoras y hay que acabar el nivel.
(defrule ceboPorPocasPildoras
   (declare (salience 20))
   (MSPACMAN (pocasPildoras true) (hayCebo true) (ceboEnfriamiento false))
   =>
   (assert (CEBO)))

;; Seguir en el cebo: en la HFSM, una vez en PrepararCebo se seguía allí
;; aunque el valor bajara; solo se salía por CeboAgotadoTransition.
(defrule sigueEnCebo
   (declare (salience 20))
   (MSPACMAN (ultima PrepararCebo) (hayCebo true) (ceboEnfriamiento false))
   =>
   (assert (CEBO)))

;; CeboAgotadoTransition, primera razón: lleva SIN_VALOR_MAX turnos esperando
;; sin que la cadena valga VALOR_MIN_CEBO. Con pocas píldoras no se abandona.
;; Como en la HFSM, solo si se sigue en RECOLECTAR (sin PELIGRO ni PODER).
(defrule ceboSinValor
   (declare (salience 5))
   (MSPACMAN (ultima PrepararCebo) (pocasPildoras false)
             (ticksSinValorCebo ?s&:(>= ?s ?*SIN_VALOR_MAX*)))
   (not (PELIGRO))
   (not (PODER))
   =>
   (assert (CEBO_AGOTADO)))

;; CeboAgotadoTransition, segunda razón: la píldora de cebo ha dejado de ser
;; candidata (se ha vuelto insegura o ya no queda ninguna).
(defrule ceboDesaparecido
   (declare (salience 5))
   (MSPACMAN (ultima PrepararCebo) (hayCebo false))
   (not (PELIGRO))
   (not (PODER))
   =>
   (assert (CEBO_AGOTADO)))

;; PrepararCebo, patrullando: esperar a RADIO_PATRULLA de la píldora.
(defrule prepararCebo
   (CEBO)
   (not (CEBO_AGOTADO))
   (not (PELIGRO))
   (not (PODER))
   =>
   (assert (ACTION (id PrepararCebo) (modo patrullar)
                   (info "prepararCebo: esperando a que se acerquen")
                   (priority 40))))

;; PrepararCebo, comer ya: la cadena vale UMBRAL_CADENA (3 fantasmas).
(defrule comerCeboYa
   (CEBO)
   (not (CEBO_AGOTADO))
   (not (PELIGRO))
   (not (PODER))
   (MSPACMAN (valorCebo ?v&:(>= ?v ?*UMBRAL_CADENA*)))
   =>
   (assert (ACTION (id PrepararCebo) (modo comer)
                   (info (str-cat "comerCeboYa: la cadena vale " ?v))
                   (priority 45))))

;; PrepararCebo, comer por paciencia: lleva PACIENCIA_CEBO turnos esperando y
;; la cadena vale lo mínimo, o quedan pocas píldoras.
(defrule comerCeboPorPaciencia
   (CEBO)
   (not (CEBO_AGOTADO))
   (not (PELIGRO))
   (not (PODER))
   (MSPACMAN (ticksEnCebo ?e&:(>= ?e ?*PACIENCIA_CEBO*)) (valorCebo ?v) (pocasPildoras ?p))
   (test (or (>= ?v ?*VALOR_MIN_CEBO*) (eq ?p true)))
   =>
   (assert (ACTION (id PrepararCebo) (modo comer)
                   (info (str-cat "comerCeboPorPaciencia: " ?e " turnos esperando"))
                   (priority 44))))
