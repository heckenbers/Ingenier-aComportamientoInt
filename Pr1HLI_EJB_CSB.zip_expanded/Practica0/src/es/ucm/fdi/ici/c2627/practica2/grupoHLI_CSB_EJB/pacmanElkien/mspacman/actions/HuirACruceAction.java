package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Huye al cruce más lejano del fantasma peligroso más cercano (getFarthestNodeIndexFromNodeIndex con DM.PATH
 * sobre getJunctionIndices). Si el camino hasta él lo cortase algún fantasma, elige el movimiento más seguro.
 */
public class HuirACruceAction extends MsPacManAction {

    public HuirACruceAction() {
        super("HuirACruce");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        GHOST g = m.getNearestDangerous();
        if (g == null) {
            return movimientoSeguro(m, true);
        }
        int nodoFantasma = m.getGhostNode(g);
        if (nodoFantasma >= 0) {
            Game game = m.getGame();
            int objetivo = game.getFarthestNodeIndexFromNodeIndex(nodoFantasma, game.getJunctionIndices(), DM.PATH);
            if (objetivo >= 0 && objetivo != m.getPacman()) {
                int[] camino = m.caminoHasta(objetivo);
                if (camino.length > 0 && !m.rutaInterceptada(camino)) {
                    MOVE mov = legal(m, m.movimientoPorCamino(camino));
                    if (mov != null) {
                        return mov;
                    }
                }
            }
        }
        return movimientoSeguro(m, true);
    }
}