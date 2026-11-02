;;; =====================================================================
;;;  Ms. Pac-Man HLI_EJB_CSB  ·  versión 0
;;;  Traducción de la HFSM de la Práctica 2: supervivencia, poder y
;;;  recolección por defecto. Sin cebo ni zona aislada (v1 y v2).
;;;
;;;  El Input (MsPacManInput) mide; estas reglas deciden; las acciones Java
;;;  calculan el movimiento. Gana el ACTION de mayor priority.
;;;
;;;  Capas:
;;;    salience 20  medir -> hechos intermedios desde MSPACMAN
;;;    salience 15  hechos intermedios que dependen de otros
;;;    salience 10  PODER (necesita saber ya si hay PELIGRO)
;;;    salience  0  reglas de acción (leen los hechos intermedios)
;;;
;;;  PorRevertirTransition NO se traduce: medido en partidas, 'revertir' nunca
;;;  baja de 21 ticks mientras hay comestibles (un comestible tan cerca acaba
;;;  comido), así que con el umbral TAU_ALERTA = 12 la transición no saltaba
;;;  nunca tampoco en la Práctica 2. El slot revertir se queda en MSPACMAN.
;;;
;;;  Franjas de priority:
;;;    80-99 supervivencia · 60-79 poder · 10-59 recolectar
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

;; Salida. id = clave del mapa de acciones Java.
(deftemplate ACTION
   (slot id)
   (slot info (default ""))
   (slot priority)
   (slot fantasma (default NONE)))   ; parámetro de CazarFantasma

;;; ---------------------------------------------------------------------
;;;  Umbrales: los mismos valores que Parametros.java
;;; ---------------------------------------------------------------------
(defglobal ?*TAU_ALERTA*    = 12)   ; Parametros.TAU_ALERTA
(defglobal ?*TAU_CRIT*      = 6)    ; Parametros.TAU_CRIT
(defglobal ?*MARGEN_SEGURO* = 4)    ; Parametros.MARGEN_SEGURO = EAT_DISTANCE + 2
(defglobal ?*HISTERESIS*    = 3)    ; Parametros.HISTERESIS

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
