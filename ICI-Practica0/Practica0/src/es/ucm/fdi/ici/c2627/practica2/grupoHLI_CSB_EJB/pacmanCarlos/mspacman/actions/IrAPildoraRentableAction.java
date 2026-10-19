package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Navegacion;
import pacman.game.Constants.MOVE;

/**
 * RECOLECTAR / IrAPíldoraRentable (subestado inicial).
 * Elige la ruta segura que más píldoras recoge, priorizando clústeres grandes
 * y cercanos, y sin pasar por píldoras de poder (se guardan para el cebo).
 */
public class IrAPildoraRentableAction extends PacManAction {

    public IrAPildoraRentableAction() {
        super("IrAPildoraRentable");
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
    }
}
