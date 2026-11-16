package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Hay un fantasma comestible a menos de 'limite' nodos (o a 'limite' o menos si 'inclusivo' es true).
 * Se usa con 40 (exclusivo) para entrar en COMER FANTASMA y con 45 (inclusivo) para seguir.
 */
public class ComestibleCercaTransition implements Transition {

    private final int limite;
    private final boolean inclusivo;

    public ComestibleCercaTransition(int limite, boolean inclusivo) {
        this.limite = limite;
        this.inclusivo = inclusivo;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        if (m.getNearestEdible() == null) {
            return false;
        }
        int d = m.getDistNearestEdible();
        return inclusivo ? d <= limite : d < limite;
    }

    @Override
    public String toString() {
        return "Comestible a " + (inclusivo ? "<= " : "< ") + limite;
    }
}