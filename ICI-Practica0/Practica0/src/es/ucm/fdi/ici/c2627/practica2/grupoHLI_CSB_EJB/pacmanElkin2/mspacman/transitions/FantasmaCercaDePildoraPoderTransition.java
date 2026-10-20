package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Algún fantasma no comestible está a 'limite' nodos o menos de la píldora de poder más cercana. Negada,
 * con 17, es la condición de 'ruta liberada' para salir de EVITAR CAMINO.
 */
public class FantasmaCercaDePildoraPoderTransition implements Transition {

    private final int limite;

    public FantasmaCercaDePildoraPoderTransition(int limite) {
        this.limite = limite;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        return m.getNearestPowerPill() >= 0 && m.distanciaMinimaFantasma(m.getNearestPowerPill()) <= limite;
    }

    @Override
    public String toString() {
        return "Fantasma a <= " + limite + " de la píldora de poder";
    }
}