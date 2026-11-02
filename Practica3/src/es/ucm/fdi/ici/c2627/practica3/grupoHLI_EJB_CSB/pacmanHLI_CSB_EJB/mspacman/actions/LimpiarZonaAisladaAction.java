package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Turno;
import pacman.game.Constants.MOVE;

/**
 * RECOLECTAR / LimpiarZonaAislada.
 * Va a por el clúster aislado más cercano mientras es barato, para no tener
 * que cruzar el laberinto al final del nivel con los fantasmas encima.
 */
public class LimpiarZonaAisladaAction extends PacManAction {

    public LimpiarZonaAisladaAction(Turno turno) {
        super("LimpiarZonaAislada", turno);
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        int[] objetivo = in.getPildorasAisladas();
        if (objetivo.length == 0)               // ya recogido: las reglas dejan de elegirla el turno siguiente
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        return Navegacion.recolectar(in, objetivo, null, 0.5);
    }
}
