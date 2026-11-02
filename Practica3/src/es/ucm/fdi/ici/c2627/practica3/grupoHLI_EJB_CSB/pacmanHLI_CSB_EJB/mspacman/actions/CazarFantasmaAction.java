package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Turno;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * PODER / CazarFantasma.
 * Persigue al fantasma que le pasa la regla en el slot 'fantasma' (el primero
 * de plan_caza, calculado en el Input: el orden que maximiza la cadena
 * 200-400-800-1600 dentro del tiempo de susto). El plan se recalcula cada
 * tick, así que se replanifica solo tras cada captura. No gasta otras
 * píldoras de poder por el camino.
 */
public class CazarFantasmaAction extends PacManAction {

    public CazarFantasmaAction(Turno turno) {
        super("CazarFantasma", turno);
    }

    /** Slot del hecho ACTION con el fantasma a perseguir: (ACTION (id CazarFantasma) (fantasma PINKY) ...). */
    public static final String SLOT_FANTASMA = "fantasma";

    private GHOST presa;

    /**
     * Acción con parámetro: la regla 'cazar' decide a quién perseguir y lo
     * pone en el slot fantasma. Si falta o no es un fantasma, presa = null.
     */
    @Override
    public void parseFact(Fact actionFact) {
        presa = null;
        try {
            presa = GHOST.valueOf(actionFact.getSlotValue(SLOT_FANTASMA).symbolValue(null));
        } catch (Exception e) {
            // slot ausente o NONE: execute recolecta y la regla dejará de dispararse
        }
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        if (presa == null)                      // no debería pasar: la regla cazar exige presa
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 2.0);
        int nodo = in.getGame().getGhostCurrentNodeIndex(presa);
        return Navegacion.irA(in, nodo, false);
    }
}
