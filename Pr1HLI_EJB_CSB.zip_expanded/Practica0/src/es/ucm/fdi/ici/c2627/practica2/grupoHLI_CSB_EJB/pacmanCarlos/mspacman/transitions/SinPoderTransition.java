package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * PODER -> RECOLECTAR.
 * Ya no queda ningún fantasma comestible (todo T_susto = 0). Se añade
 * después de las de peligro, así que ¬peligro está garantizado.
 */
public class SinPoderTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return !((MsPacManInput) in).isHayComestibles();
    }

    @Override
    public String toString() {
        return "poder expirado";
    }
}
