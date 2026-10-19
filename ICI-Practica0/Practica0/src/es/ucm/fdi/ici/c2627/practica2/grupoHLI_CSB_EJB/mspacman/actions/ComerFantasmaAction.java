package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Persigue al fantasma comestible más cercano por la ruta más corta.
 */
public class ComerFantasmaAction extends MsPacManAction {

    public ComerFantasmaAction() {
        super("ComerFantasma");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        GHOST g = m.getNearestEdible();
        if (g == null) {
            return movimientoSeguro(m, false);
        }
        MOVE mov = legal(m, m.movimientoHacia(m.getGhostNode(g)));
        return mov != null ? mov : movimientoSeguro(m, false);
    }
}