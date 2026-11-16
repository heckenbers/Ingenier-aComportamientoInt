package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.Turno;
import pacman.game.Constants.MOVE;

/**
 * RECOLECTAR / IrAPíldoraRentable (subestado inicial).
 * Elige la ruta segura que más píldoras recoge, priorizando clústeres grandes
 * y cercanos, y sin pasar por píldoras de poder (se guardan para el cebo).
 */
public class IrAPildoraRentableAction extends PacManAction {

    public IrAPildoraRentableAction(Turno turno) {
        super("IrAPildoraRentable", turno);
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
    }
}
