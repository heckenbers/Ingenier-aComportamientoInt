package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Navegacion;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * PODER / CazarFantasma.
 * Persigue el primer fantasma de plan_caza (el orden que maximiza la cadena
 * 200-400-800-1600 dentro del tiempo de susto). El plan se recalcula cada
 * tick, así que se replanifica solo tras cada captura. No gasta otras
 * píldoras de poder por el camino.
 */
public class CazarFantasmaAction extends PacManAction {

    public CazarFantasmaAction() {
        super("CazarFantasma");
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        GHOST presa = in.getPresa();
        if (presa == null)                      // la transición saldrá en el siguiente tick
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 2.0);
        int nodo = in.getGame().getGhostCurrentNodeIndex(presa);
        return Navegacion.irA(in, nodo, false);
    }
}
