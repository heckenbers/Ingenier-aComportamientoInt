;;; v2 · primera versión del grupo: cambia SOLO cómo huyen (frente a v1)
;;;   - comestible: huidaEficiente (pasillos, dos cruces, tiempo comestible) en vez de huir
;;;   - PP amenazada: huirDePP (anticipa la inversión de sentido) en vez de huir

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


;; ==========================================================================================
;; Umbrales de esta versión (antes constantes de Java; ahora viven en las reglas)
;;   RADIO_PILL    = 35  Ms. Pac-Man a esta distancia o menos de su PP: la PP está amenazada
;;   RADIO_PELIGRO = 50  el fantasma a esta distancia o menos de esa PP: está en peligro
;; ==========================================================================================

;; G1 · por defecto: si no soy comestible, perseguir                       (igual que v1)
(defrule perseguir
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false))
   =>
   (assert (ACTION (id perseguir) (info "G1: no soy comestible") (priority 10))))

;; G2 · supervivencia: si soy comestible, huida eficiente                  (v1: huir)
(defrule huirComestible
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible true))
   =>
   (assert (ACTION (id huidaEficiente) (info "G2: soy comestible") (priority 50))))

;; G3 · supervivencia: PP amenazada y yo cerca de ella -> apartarme a tiempo   (v1: huir si distPPill < 15)
;;      Solo los no comestibles: un comestible ya lo cubre G2.
(defrule huirDeLaPPill
   (YO (nombre ?yo))
   (MSPACMAN (distPPill ?p&:(<= ?p 35)))
   (FANTASMA (nombre ?yo) (comestible false) (distPP ?d&:(<= ?d 50)))
   =>
   (assert (ACTION (id huirDePP) (info "G3: PP amenazada y estoy cerca") (priority 40))))
