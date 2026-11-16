package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * La píldora más cercana está vigilada por un fantasma: se patrulla la zona segura. Entre los cruces a los que
 * Ms. Pac-Man llega con ventaja sobre todos los fantasmas, va al que queda más lejos de ellos (en caso de
 * empate, al más cercano). Si no hay ninguno, el movimiento más seguro.
 */
public class PatrullarAction extends MsPacManAction {

    public PatrullarAction() {
        super("Patrullar");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        int mejorCruce = -1;
        int mejorSeguridad = -1;
        int mejorDistancia = Constantes.SIN_DISTANCIA;
        for (int cruce : m.getGame().getJunctionIndices()) {
            if (cruce == m.getPacman()) {
                continue;
            }
            int mia = m.distanciaPacman(cruce);
            if (mia <= 0) {
                continue;
            }
            int fantasma = m.distanciaMinimaFantasma(cruce);
            if (fantasma != Constantes.SIN_DISTANCIA && fantasma < mia + Constantes.PATRULLA_VENTAJA) {
                continue;                                  // un fantasma llega antes o casi a la vez
            }
            int seguridad = Math.min(fantasma, Constantes.DISTANCIA_SEGURA);
            if (seguridad > mejorSeguridad || (seguridad == mejorSeguridad && mia < mejorDistancia)) {
                mejorSeguridad = seguridad;
                mejorDistancia = mia;
                mejorCruce = cruce;
            }
        }
        if (mejorCruce >= 0) {
            int[] camino = m.caminoHasta(mejorCruce);
            if (camino.length > 0 && !m.rutaInterceptada(camino)) {
                MOVE mov = legal(m, m.movimientoPorCamino(camino));
                if (mov != null) {
                    return mov;
                }
            }
        }
        return movimientoSeguro(m, true);
    }
}