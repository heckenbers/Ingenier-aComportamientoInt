package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Hay un fantasma no comestible a menos de 'limite' nodos de la píldora común más cercana: está vigilada
 * y conviene patrullar la zona segura en vez de ir a por ella.
 */
public class PildoraVigiladaTransition implements Transition {

    private final int limite;

    public PildoraVigiladaTransition(int limite) {
        this.limite = limite;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        return m.getNearestPill() >= 0 && m.distanciaMinimaFantasma(m.getNearestPill()) < limite;
    }

    @Override
    public String toString() {
        return "Píldora vigilada a < " + limite;
    }
}