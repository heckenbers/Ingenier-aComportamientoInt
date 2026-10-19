package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * Va hacia la píldora de poder activa más cercana por la ruta más corta.
 */
public class IrPildoraPoderAction extends MsPacManAction {

    public IrPildoraPoderAction() {
        super("IrPildoraPoder");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        int pildora = m.getNearestPowerPill();
        if (pildora < 0) {
            return movimientoSeguro(m, false);
        }
        MOVE mov = legal(m, m.movimientoHacia(pildora));
        return mov != null ? mov : movimientoSeguro(m, false);
    }
}