package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * Hay una píldora de poder cerca pero ningún fantasma comestible que cazar: se elige un movimiento que no la
 * pise, para no perder el beneficio del poder. Entre los demás gana el más seguro.
 */
public class EsquivarPildoraPoderAction extends MsPacManAction {

    public EsquivarPildoraPoderAction() {
        super("EsquivarPildoraPoder");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        return movimientoSeguro(m, true);
    }
}