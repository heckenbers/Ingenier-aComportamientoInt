package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;

/** Se cumplen las dos condiciones (la segunda solo se calcula si se cumple la primera). */
public class AndTransition implements Transition {

    private final Transition a;
    private final Transition b;

    public AndTransition(Transition a, Transition b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean evaluate(Input in) {
        return a.evaluate(in) && b.evaluate(in);
    }

    @Override
    public String toString() {
        return "(" + a + ") Y (" + b + ")";
    }
}