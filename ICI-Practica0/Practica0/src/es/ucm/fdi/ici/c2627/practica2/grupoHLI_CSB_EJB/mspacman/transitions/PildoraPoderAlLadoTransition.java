package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Hay una píldora de poder a 'limite' nodos o menos, quedan píldoras comunes que comer y no hay ningún
 * fantasma comestible que cazar: hay que esquivarla para no perder el beneficio del poder.
 */
public class PildoraPoderAlLadoTransition implements Transition {

    private final int limite;

    public PildoraPoderAlLadoTransition(int limite) {
        this.limite = limite;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        return m.getActivePills().length > 0 && m.getNearestEdible() == null
                && m.getNearestPowerPill() >= 0 && m.getDistNearestPowerPill() <= limite;
    }

    @Override
    public String toString() {
        return "Píldora de poder al lado a <= " + limite + " sin comestibles";
    }
}