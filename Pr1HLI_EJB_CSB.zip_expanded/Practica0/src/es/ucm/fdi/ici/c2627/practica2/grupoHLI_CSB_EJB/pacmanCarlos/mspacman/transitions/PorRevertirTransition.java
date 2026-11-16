package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.Parametros;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * PODER -> SUPERVIVENCIA.
 * por_revertir: un fantasma comestible volverá a ser peligroso en la posición
 * de Pac-Man en <= TAU_ALERTA ticks (se le acaba el susto y está cerca).
 */
public class PorRevertirTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return ((MsPacManInput) in).isPorRevertir();
    }

    @Override
    public String toString() {
        return "comestible por revertir (<= " + Parametros.TAU_ALERTA + " ticks)";
    }
}
