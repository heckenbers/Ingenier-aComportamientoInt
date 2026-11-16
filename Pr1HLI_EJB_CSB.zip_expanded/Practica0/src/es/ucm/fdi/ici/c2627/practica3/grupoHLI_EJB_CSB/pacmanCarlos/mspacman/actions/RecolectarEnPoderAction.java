package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.Turno;
import pacman.game.Constants.MOVE;

/**
 * PODER / RecolectarEnPoder.
 * No hay presa alcanzable: come píldoras dando más peso al margen, porque la
 * amenaza ya incluye a los comestibles que van a dejar de serlo. Así, cuando
 * se acaba el susto, Pac-Man ya está lejos de ellos.
 */
public class RecolectarEnPoderAction extends PacManAction {

    public RecolectarEnPoderAction(Turno turno) {
        super("RecolectarEnPoder", turno);
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 2.0);
    }
}
