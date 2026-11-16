package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * Evalúa exactamente la misma condición que la transición que envuelve; solo cambia el texto. El grafo del
 * observador usa el toString() de la transición como identificador de la flecha, y dentro de una máquina
 * no puede haber dos iguales: cuando la misma condición sale de varios estados, se distingue por el origen.
 */
public class EtiquetadaTransition implements Transition {

    private final Transition t;
    private final String origen;

    public EtiquetadaTransition(Transition t, String origen) {
        this.t = t;
        this.origen = origen;
    }

    @Override
    public boolean evaluate(Input in) {
        return t.evaluate(in);
    }

    @Override
    public String toString() {
        return t + " [desde " + origen + "]";
    }
}