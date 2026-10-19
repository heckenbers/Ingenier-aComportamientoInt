package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Navegacion;
import pacman.game.Constants.MOVE;

/**
 * RECOLECTAR / LimpiarZonaAislada.
 * Va a por el clúster aislado más cercano mientras es barato, para no tener
 * que cruzar el laberinto al final del nivel con los fantasmas encima.
 */
public class LimpiarZonaAisladaAction extends PacManAction {

    public LimpiarZonaAisladaAction() {
        super("LimpiarZonaAislada");
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        int[] objetivo = in.getPildorasAisladas();
        if (objetivo.length == 0)               // ya recogido: la transición saldrá en el siguiente tick
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        return Navegacion.recolectar(in, objetivo, null, 0.5);
    }
}
