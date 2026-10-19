package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;

/** RecolectarEnPoder -> CazarFantasma: aparece una presa alcanzable. */
public class PlanCazaDisponibleTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return ((MsPacManInput) in).getPresa() != null;
    }

    @Override
    public String toString() {
        return "presa alcanzable";
    }
}
