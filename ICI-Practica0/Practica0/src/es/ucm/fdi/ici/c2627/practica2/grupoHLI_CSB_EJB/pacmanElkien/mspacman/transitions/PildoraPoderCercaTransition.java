package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * La píldora de poder activa más cercana está a 'limite' nodos o menos. Si 'requierePeligroso' es true,
 * además tiene que haber un fantasma no comestible fuera de la cárcel: sin peligro no se va a por ella,
 * se esquiva para no gastarla.
 */
public class PildoraPoderCercaTransition implements Transition {

    private final int limite;
    private final boolean requierePeligroso;

    public PildoraPoderCercaTransition(int limite, boolean requierePeligroso) {
        this.limite = limite;
        this.requierePeligroso = requierePeligroso;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        if (m.getNearestPowerPill() < 0 || m.getDistNearestPowerPill() > limite) {
            return false;
        }
        return !requierePeligroso || m.getNearestDangerous() != null;
    }

    @Override
    public String toString() {
        return "Píldora de poder a <= " + limite + (requierePeligroso ? " con peligro" : "");
    }
}