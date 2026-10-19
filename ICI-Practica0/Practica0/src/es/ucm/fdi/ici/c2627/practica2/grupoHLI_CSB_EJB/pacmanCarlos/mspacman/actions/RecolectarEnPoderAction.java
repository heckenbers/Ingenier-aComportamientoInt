package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Navegacion;
import pacman.game.Constants.MOVE;

/**
 * PODER / RecolectarEnPoder.
 * No hay presa alcanzable: come píldoras dando más peso al margen, porque la
 * amenaza ya incluye a los comestibles que van a dejar de serlo. Así, cuando
 * se acaba el susto, Pac-Man ya está lejos de ellos.
 */
public class RecolectarEnPoderAction extends PacManAction {

    public RecolectarEnPoderAction() {
        super("RecolectarEnPoder");
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 2.0);
    }
}
