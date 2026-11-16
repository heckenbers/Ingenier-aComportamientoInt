package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * Va hacia la píldora común más cercana por la ruta más corta, sin pasar por una píldora de poder (para no
 * gastarla) y sin que ningún fantasma corte la ruta; si la ruta está cortada, recalcula con la siguiente
 * píldora más cercana.
 */
public class ComerPildoraAction extends MsPacManAction {

    public ComerPildoraAction() {
        super("ComerPildora");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        // Si no quedan píldoras comunes, las de poder son lo único que falta para acabar el nivel
        boolean quedanComunes = m.getActivePills().length > 0;
        int[] objetivos = quedanComunes ? m.getActivePills() : m.getActivePowerPills();
        for (int objetivo : m.masCercanas(objetivos, Constantes.NUM_CANDIDATAS_PILDORA)) {
            int[] camino = m.caminoHasta(objetivo);
            if (camino.length == 0 || m.rutaInterceptada(camino)) {
                continue;                                  // ruta cortada: se recalcula con otra píldora
            }
            if (quedanComunes && m.contienePildoraPoder(camino)) {
                continue;                                  // pasaría por una píldora de poder: se esquiva
            }
            MOVE mov = legal(m, m.movimientoPorCamino(camino));
            if (mov != null) {
                return mov;
            }
        }
        return movimientoSeguro(m, quedanComunes);
    }
}