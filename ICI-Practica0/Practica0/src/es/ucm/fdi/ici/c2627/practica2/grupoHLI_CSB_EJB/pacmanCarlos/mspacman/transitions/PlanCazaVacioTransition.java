package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;

/** CazarFantasma -> RecolectarEnPoder: ningún fantasma comestible es alcanzable a tiempo. */
public class PlanCazaVacioTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return ((MsPacManInput) in).getPresa() == null;
    }

    @Override
    public String toString() {
        return "plan_caza vacío";
    }
}
