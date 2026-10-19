package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * Huye hacia la píldora de poder cuando Ms. Pac-Man llega antes que los fantasmas: si el camino no pasa cerca
 * de ninguno, va por él (la transición ya lo ha comprobado, aquí se vuelve a hacer por seguridad); si no, el
 * movimiento más seguro.
 */
public class HuirHaciaPildoraPoderAction extends MsPacManAction {

    public HuirHaciaPildoraPoderAction() {
        super("HuirHaciaPildoraPoder");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        int pildora = m.getNearestPowerPill();
        if (pildora >= 0) {
            int[] camino = m.caminoHasta(pildora);
            if (camino.length > 0 && !m.rutaInterceptada(camino)
                    && m.distanciaMinimaFantasmaEnRuta(camino) >= Constantes.HUIR_PODER_DISTANCIA_RUTA) {
                MOVE mov = legal(m, m.movimientoPorCamino(camino));
                if (mov != null) {
                    return mov;
                }
            }
        }
        return movimientoSeguro(m, false);
    }
}