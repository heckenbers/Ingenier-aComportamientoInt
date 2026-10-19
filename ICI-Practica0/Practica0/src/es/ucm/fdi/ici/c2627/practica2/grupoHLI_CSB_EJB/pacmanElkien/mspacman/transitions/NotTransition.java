package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;

/** Se cumple cuando no se cumple la condición dada. */
public class NotTransition implements Transition {

    private final Transition t;

    public NotTransition(Transition t) {
        this.t = t;
    }

    @Override
    public boolean evaluate(Input in) {
        return !t.evaluate(in);
    }

    @Override
    public String toString() {
        return "NO (" + t + ")";
    }
}