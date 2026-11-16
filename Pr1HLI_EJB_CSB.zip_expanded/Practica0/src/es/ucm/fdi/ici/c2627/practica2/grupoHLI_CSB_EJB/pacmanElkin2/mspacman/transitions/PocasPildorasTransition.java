package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Quedan menos de 'cuantas' píldoras comunes sin comer (final de nivel: conviene limpiar con el recorrido
 * más corto en vez de ir a la más cercana).
 */
public class PocasPildorasTransition implements Transition {

    private final int cuantas;

    public PocasPildorasTransition(int cuantas) {
        this.cuantas = cuantas;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        return m.getActivePills().length < cuantas;
    }

    @Override
    public String toString() {
        return "Quedan menos de " + cuantas + " píldoras";
    }
}