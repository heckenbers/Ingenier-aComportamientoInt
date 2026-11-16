package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.Parametros;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * Evadir -> EscapeDeEmergencia.
 * crítico: un fantasma llega en <= TAU_CRIT ticks o no hay salida segura
 * (detecta las pinzas de fantasmas cooperativos).
 */
public class CriticoTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return ((MsPacManInput) in).isCritico();
    }

    @Override
    public String toString() {
        return "crítico (<= " + Parametros.TAU_CRIT + " ticks o sin salida)";
    }
}
