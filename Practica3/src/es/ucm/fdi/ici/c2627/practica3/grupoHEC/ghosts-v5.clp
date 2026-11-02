;;; v6 · = v5 + VIGILAR LA PP (antes nadie pensaba en la PP hasta que ella estaba a 35)
;;;   Un solo cambio frente a v5, en cómo se protege la PP:
;;;   - cuando ella se va acercando a su PP (36..70), el fantasma que mejor llega a ella
;;;     (sin contar al perseguidor) se adelanta y se coloca junto a la PP (G15);
;;;   - sigue vigilando mientras siga llegando antes y ella no se aleje de 80 (G16);
;;;   - cuando ella entra en el radio de 35, el vigilante ya está allí con margen de sobra
;;;     y G4 (bloquear) toma el relevo. Antes, sin nadie cerca, todos huían (G3).

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
;;   Comestibles (v5), por distPeligro (lo cerca que está ella de comérselo):
;;     > 40          dispersarse      (seguir dispersándose mientras > 30)
;;     <= 40         refugiarse, si GhostsInput encontró un compañero seguro
;;                                    (seguir refugiándose mientras <= 50)
;;     resto         huidaEficiente (G2)
;;     volver a cazar si distPeligro > 2·tiempoComestible + 4 y ella no está junto a una PP:
;;       acercándose los dos, la distancia baja como mucho 1,5 nodos por turno (ella 1, él 0,5),
;;       así que deja de ser comestible antes del encuentro; el +4 es la holgura de MARGEN_HUIR.
;;
;;   Vigilancia (v6): ella entre 36 y 70 de su PP (seguir hasta 80), margenBloqueo > 0
;;
;; Prioridades: perseguir 10 < cazar 15 < interceptar 20 < vigilarPP 22 < escoltar 25 < cerrarPaso 30
;;              < huirDePP 40 < bloquearPP 45 < huidaEficiente 50 < dispersarse 53
;;              < refugiarse 55 < seguir (histéresis) 56 < volverACazar 58
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

;; ==========================================================================================
;; v5 · COMESTIBLES
;;   Todas exigen (comestible true) salvo G14, que es la de su refugio (no comestible).
;;   Las de "seguir" (G10, G12) tienen más prioridad que las de "entrar" (G9, G11): así el
;;   fantasma no cambia entre dispersarse y refugiarse cada vez que ella pasa de 40.
;; ==========================================================================================

;; G9 · ella lejos: dispersarse a una esquina distinta de la de los demás comestibles
(defrule dispersarse
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible true) (distPeligro ?dp&:(> ?dp 40)))
   =>
   (assert (ACTION (id dispersarse) (info "G9: comestible y ella lejos") (priority 53))))

;; G10 · seguir dispersándose (histéresis): ya me dispersaba y ella no se ha acercado a 30
(defrule sigueDispersandose
   (YO (nombre ?yo))
   (ULTIMA (accion dispersarse))
   (FANTASMA (nombre ?yo) (comestible true) (distPeligro ?dp&:(> ?dp 30)))
   =>
   (assert (ACTION (id dispersarse) (info "G10: ya me dispersaba y sigue lejos") (priority 56))))

;; G11 · refugiarse: ella no está lejos y tengo un compañero no comestible al que llego antes
;;       que ella (lo calcula GhostsInput). El compañero viaja como parámetro del ACTION.
(defrule refugiarse
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible true) (distPeligro ?dp&:(<= ?dp 40)) (companero ?c&~NINGUNO))
   =>
   (assert (ACTION (id refugiarse) (companero ?c) (info "G11: me refugio en un compañero") (priority 55))))

;; G12 · seguir refugiándose (histéresis): ya iba hacia mi compañero, sigue siendo seguro
;;       (companero no es NINGUNO) y ella no se ha alejado más de 50
(defrule sigueRefugiado
   (YO (nombre ?yo))
   (ULTIMA (accion refugiarse))
   (FANTASMA (nombre ?yo) (comestible true) (distPeligro ?dp&:(<= ?dp 50)) (companero ?c&~NINGUNO))
   =>
   (assert (ACTION (id refugiarse) (companero ?c) (info "G12: ya me refugiaba y sigo") (priority 56))))

;; G13 · volver a cazar: me queda tan poco de comestible que, aunque vaya hacia ella, dejo de
;;       serlo antes de que me alcance. No si ella está junto a otra PP (me volvería comestible).
(defrule volverACazar
   (YO (nombre ?yo))
   (MSPACMAN (distPPill ?p&:(> ?p 35)))
   (FANTASMA (nombre ?yo) (comestible true) (tiempoComestible ?t) (distPeligro ?dp))
   (test (> ?dp (+ (* 2 ?t) 4)))
   =>
   (assert (ACTION (id cazar) (info "G13: casi no me queda de comestible") (priority 58))))

;; G14 · escoltar: soy el refugio de un comestible -> salgo a su encuentro (ella viene detrás
;;       de él, así que me la encuentro de frente). Reutiliza 'refugiarse' con el comestible
;;       como parámetro. Relaciona dos FANTASMA: el companero del otro soy yo.
(defrule escoltar
   (YO (nombre ?yo))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false))
   (FANTASMA (nombre ?c) (comestible true) (companero ?yo))
   =>
   (assert (ACTION (id refugiarse) (companero ?c) (info "G14: un comestible se refugia en mí") (priority 25))))

;; ==========================================================================================
;; v6 · VIGILAR LA PP
;;   La vigilancia es ANTES de la amenaza (distPPill > 35). Con la PP amenazada mandan G3-G5:
;;   el vigilante, ya junto a la PP, tiene el mayor margenBloqueo y G4 lo pone a bloquear.
;; ==========================================================================================

;; G15 · vigilar la PP: ella se acerca a su PP, no soy el perseguidor, llego a la PP antes
;;       que ella y nadie (salvo el perseguidor, que sigue detrás de ella) llega con más margen
(defrule vigilarPP
   (YO (nombre ?yo))
   (MSPACMAN (distPPill ?p&:(> ?p 35)&:(<= ?p 70)))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (margenBloqueo ?m&:(> ?m 0)))
   (PERSEGUIDOR (nombre ?per&~?yo))
   (not (FANTASMA (nombre ?otro&~?yo&~?per) (comestible false) (enCarcel false)
                  (margenBloqueo ?m2&:(> ?m2 ?m))))
   =>
   (assert (ACTION (id vigilarPP) (info "G15: ella va hacia la PP y yo llego antes") (priority 22))))

;; G16 · seguir vigilando (histéresis): ya vigilaba, sigo llegando antes y ella no se ha
;;       alejado de 80, aunque ahora otro tenga más margen. Cambiar de vigilante a medio
;;       camino deja la PP sin nadie.
(defrule sigueVigilando
   (YO (nombre ?yo))
   (ULTIMA (accion vigilarPP))
   (MSPACMAN (distPPill ?p&:(> ?p 35)&:(<= ?p 80)))
   (FANTASMA (nombre ?yo) (comestible false) (enCarcel false) (margenBloqueo ?m&:(> ?m 0)))
   =>
   (assert (ACTION (id vigilarPP) (info "G16: ya vigilaba y sigo llegando antes") (priority 22))))
