;;; v4 · = v3 + CAZA COORDINADA (antes los cuatro iban directos a por ella)
;;;   Un solo cambio frente a v3, en cómo cazan los no comestibles:
;;;   - un hecho intermedio PERSEGUIDOR: el más cercano a ella persigue (G1, como siempre);
;;;   - los demás van por otros caminos (G6 cazar), y el que puede adelantarse a ella le
;;;     corta el paso (G7 interceptar);
;;;   - si ella está en un pasillo sin PP, el que llega antes a su salida lo cierra (G8).

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
;;   Intercepción: margenCorte >= 2 (se adelanta al menos 2 turnos en algún punto de su camino)
;;   Cierre: margenCierre >= 0 (llega a la salida del pasillo de ella cuando ella o antes)
;;
;; Prioridades: perseguir 10 < cazar 15 < interceptar 20 < cerrarPaso 30
;;              < huirDePP 40 < bloquearPP 45 < huidaEficiente 50
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

;; ==========================================================================================
;; v4 · CAZA COORDINADA
;; ==========================================================================================

;; Hecho intermedio: quién es el perseguidor (el no comestible más cercano a Ms. Pac-Man).
(deftemplate PERSEGUIDOR (slot nombre (type SYMBOL)))

;; P · elegir al perseguidor. Con salience, para que exista antes de que miren las reglas de caza.
(defrule elegirPerseguidor
   (declare (salience 10))
   (FANTASMA (nombre ?g) (comestible false) (enCarcel false) (distPacman ?d))
   (not (FANTASMA (comestible false) (enCarcel false) (distPacman ?d2&:(< ?d2 ?d))))
   =>
   (assert (PERSEGUIDOR (nombre ?g))))

;; G6 · si no soy el perseguidor, cazar por otro camino (evita los caminos de los demás)
(defrule cazarPorOtroLado
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false))
   (PERSEGUIDOR (nombre ?p&~?yo))
   =>
   (assert (ACTION (id cazar) (info "G6: otro persigue, yo voy por otro camino") (priority 15))))

;; G7 · interceptar: no soy el perseguidor, puedo adelantarme a ella en su camino y nadie
;;      (salvo el perseguidor) se adelanta más que yo
(defrule interceptar
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (margenCorte ?c&:(>= ?c 2)))
   (PERSEGUIDOR (nombre ?p&~?yo))
   (not (FANTASMA (nombre ?otro&~?yo&~?p) (comestible false) (enCarcel false)
                  (margenCorte ?c2&:(> ?c2 ?c))))
   =>
   (assert (ACTION (id interceptar) (info "G7: puedo adelantarme a ella") (priority 20))))

;; G8 · cerrar paso: ella está en un pasillo sin PP (no puede darse la vuelta) y yo llego a
;;      la salida cuando ella o antes, mejor que nadie. Vale para cualquier papel.
(defrule cerrarPaso
   (YO (nombre ?yo))
   (MSPACMAN (enPasillo true))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (margenCierre ?m&:(>= ?m 0)))
   (not (FANTASMA (nombre ?otro&~?yo) (comestible false) (enCarcel false)
                  (margenCierre ?m2&:(> ?m2 ?m))))
   =>
   (assert (ACTION (id cerrarPaso) (info "G8: la atrapo en su pasillo") (priority 30))))
