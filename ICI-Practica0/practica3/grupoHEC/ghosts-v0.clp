;;; Los cuatro fantasmas con la misma base de reglas: la v0, con G1 y G2
;;; (plantillas actualizadas a los hechos de GhostsInput; las reglas son las originales)

;;; Plantillas de los hechos de los fantasmas (los que aserta GhostsInput y Ghosts).
;;; Jess solo carga un .clp por motor, así que este bloque se COPIA al principio de cada
;;; ghosts-vN.clp. Si un hecho no encaja con su plantilla, Jess no lo aserta (y no avisa).

;; ---- Un hecho por fantasma, iguales en los cuatro motores ----------------------------
(deftemplate FANTASMA
   (slot nombre           (type SYMBOL))
   (slot comestible       (type SYMBOL))   ; true / false (en minúscula, como lo escribe Java)
   (slot enCarcel         (type SYMBOL))
   (slot tiempoComestible (type NUMBER))   ; turnos que le quedan de comestible (0 si no lo es)
   (slot distPacman       (type NUMBER))   ; del fantasma a ella, sin darse la vuelta: para CAZAR
   (slot distPeligro      (type NUMBER))   ; lo cerca que está ella de comérselo: para HUIR
   (slot distPP           (type NUMBER))   ; del fantasma a la PP más cercana a Ms. Pac-Man
   (slot margenBloqueo    (type NUMBER))   ; turnos que se adelanta a ella en esa PP (>0: llega antes)
   (slot margenCorte      (type NUMBER))   ; turnos que se adelanta a ella en algún punto de su camino
   (slot margenCierre     (type NUMBER))   ; ventaja para cerrar el pasillo de ella (-1000 si no se puede)
   (slot companero        (type SYMBOL)))  ; compañero no comestible en el que refugiarse, o NINGUNO

;; ---- Ms. Pac-Man -----------------------------------------------------------------------
(deftemplate MSPACMAN
   (slot distPPill (type NUMBER))          ; de ella a su PP más cercana (1000 si no queda)
   (slot enPasillo (type SYMBOL)))         ; true: está en un pasillo sin PP, se la puede atrapar

;; ---- Distintos en cada motor -------------------------------------------------------------
(deftemplate YO     (slot nombre (type SYMBOL)))   ; quién soy
(deftemplate ULTIMA (slot accion (type SYMBOL)))   ; la acción que ganó en mi turno anterior

;; ---- Salida ------------------------------------------------------------------------------
(deftemplate ACTION
   (slot id) (slot info (default "")) (slot priority (type NUMBER))
   (slot companero (type SYMBOL) (default NINGUNO)))   ; parámetro de 'refugiarse'


;; G1 · si no soy comestible, perseguir
(defrule perseguir
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false))
   =>
   (assert (ACTION (id perseguir) (info "G1: no soy comestible") (priority 10))))

;; G2 · si soy comestible, huir
(defrule huirComestible
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible true))
   =>
   (assert (ACTION (id huir) (info "G2: soy comestible") (priority 50))))