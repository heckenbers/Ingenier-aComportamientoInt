package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * cazar (CazarFantasma de la V3): a por el primer fantasma del plan de caza, el orden que
 * más puntos encadena antes de que se acabe el susto. Sin presa alcanzable, come dando más
 * peso al margen (RecolectarEnPoder de la V3).
 */
public class CazarAction extends AccionPacMan {

    public CazarAction(Contexto contexto) {
        super("cazar", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        GHOST presa = in.getPresa();
        if (presa == null)
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 2.0);
        return Navegacion.irA(in, in.getGame().getGhostCurrentNodeIndex(presa), false);
    }
}
