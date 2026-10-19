package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Parametros;

/**
 * RECOLECTAR -> SUPERVIVENCIA y PODER -> SUPERVIVENCIA.
 * peligro: un fantasma no comestible llega en <= TAU_ALERTA ticks,
 * o ninguna ruta deja a Pac-Man con ventaja suficiente (no hay salida segura).
 */
public class PeligroTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return ((MsPacManInput) in).isPeligro();
    }

    @Override
    public String toString() {
        return "peligro (fantasma a <= " + Parametros.TAU_ALERTA + " ticks o sin salida segura)";
    }
}
