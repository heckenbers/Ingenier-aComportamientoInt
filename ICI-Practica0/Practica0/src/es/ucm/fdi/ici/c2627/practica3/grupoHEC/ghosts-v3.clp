;;; v3 · = v2 + BLOQUEAR LA PP (G4 entrar, G5 seguir)
;;;   Un solo cambio frente a v2: el fantasma que llega a la PP amenazada con ventaja de sobra,
;;;   y mejor situado que los demás, va a ponerse en medio en vez de huir.

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
;;   Bloqueo: entrar con margenBloqueo > 3 (MARGEN_PP 0 + HISTERESIS_BLOQUEO 3);
;;            seguir mientras margenBloqueo > 0 (todavía llega antes que ella)
;;            y la PP siga amenazada con algo de holgura (distPPill <= 35 + 3)
;;
;; Prioridades: perseguir 10 < huirDePP 40 < bloquearPP 45 < huidaEficiente 50
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

;; G4 · oportunidad: BLOQUEAR LA PP (entrar)
;;      La PP está amenazada, llego a ella con más de 3 turnos de ventaja sobre Ms. Pac-Man
;;      y NO hay otro fantasma no comestible con más ventaja (not + variables que relacionan
;;      dos FANTASMA). Así no se lanzan todos a bloquear.
(defrule bloquearPP
   (YO (nombre ?yo))
   (MSPACMAN (distPPill ?p&:(<= ?p 35)))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (margenBloqueo ?m&:(> ?m 3)))
   (not (FANTASMA (nombre ?otro&~?yo) (comestible false) (enCarcel false)
                  (margenBloqueo ?m2&:(> ?m2 ?m))))
   =>
   (assert (ACTION (id bloquearPP) (info "G4: llego antes a la PP y soy el mejor situado") (priority 45))))

;; G5 · BLOQUEAR LA PP (seguir) · histéresis con la memoria del turno anterior
;;      Si ya estaba bloqueando, sigo mientras llegue antes que ella (margen > 0), aunque ya
;;      no tenga ventaja de sobra o haya otro mejor situado: abandonar a mitad es lo peor.
(defrule sigueBloqueando
   (YO (nombre ?yo))
   (ULTIMA (accion bloquearPP))
   (MSPACMAN (distPPill ?p&:(<= ?p 38)))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (margenBloqueo ?m&:(> ?m 0)))
   =>
   (assert (ACTION (id bloquearPP) (info "G5: ya bloqueaba y sigo llegando antes") (priority 45))))
