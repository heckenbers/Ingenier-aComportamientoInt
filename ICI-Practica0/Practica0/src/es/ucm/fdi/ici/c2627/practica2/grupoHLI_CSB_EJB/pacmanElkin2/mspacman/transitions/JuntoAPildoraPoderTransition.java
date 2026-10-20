package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Ms. Pac-Man está a 'limite' nodos o menos de la píldora de poder activa más cercana. Si 'requierePeligroso'
 * es true, además hay un fantasma no comestible fuera de la cárcel.
 */
public class JuntoAPildoraPoderTransition implements Transition {

    private final int limite;
    private final boolean requierePeligroso;

    public JuntoAPildoraPoderTransition(int limite, boolean requierePeligroso) {
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
        return "Junto a la píldora de poder a <= " + limite;
    }
}