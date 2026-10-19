package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;

/**
 * RECOLECTAR -> PODER y SUPERVIVENCIA -> PODER.
 * Pac-Man ha comido una píldora de poder: hay fantasmas comestibles fuera de
 * la casa. Se exige además que no haya peligro ni reversión inminente; si no,
 * PODER saltaría a SUPERVIVENCIA en el mismo tick y los dos oscilarían.
 */
public class PoderActivoTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput input = (MsPacManInput) in;
        return input.isHayComestibles() && !input.isPeligro() && !input.isPorRevertir();
    }

    @Override
    public String toString() {
        return "poder comido (hay comestibles)";
    }
}
