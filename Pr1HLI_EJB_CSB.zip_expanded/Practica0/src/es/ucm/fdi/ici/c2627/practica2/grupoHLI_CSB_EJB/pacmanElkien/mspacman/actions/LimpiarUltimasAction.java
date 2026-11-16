package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * Quedan pocas píldoras: de las más cercanas elige la que, sumando lo que cuesta llegar y lo que queda desde
 * ella hasta la siguiente píldora, da el recorrido más corto, entre las que ningún fantasma corta. Evita las
 * píldoras de poder mientras queden comunes.
 */
public class LimpiarUltimasAction extends MsPacManAction {

    public LimpiarUltimasAction() {
        super("LimpiarUltimas");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        boolean quedanComunes = m.getActivePills().length > 0;
        int[] objetivos = quedanComunes ? m.getActivePills() : m.getActivePowerPills();
        int mejorObjetivo = -1;
        long mejorCoste = Long.MAX_VALUE;
        for (int candidato : m.masCercanas(objetivos, Constantes.NUM_CANDIDATAS_PILDORA)) {
            int[] camino = m.caminoHasta(candidato);
            if (camino.length == 0 || m.rutaInterceptada(camino)) {
                continue;
            }
            if (quedanComunes && m.contienePildoraPoder(camino)) {
                continue;
            }
            int hasta = m.distanciaPacman(candidato);
            if (hasta < 0) {
                continue;
            }
            int siguiente = Constantes.SIN_DISTANCIA;
            for (int otro : objetivos) {
                if (otro == candidato) {
                    continue;
                }
                int d = m.distanciaEntre(candidato, otro);
                if (d >= 0 && d < siguiente) {
                    siguiente = d;
                }
            }
            long coste = (long) hasta + (siguiente == Constantes.SIN_DISTANCIA ? 0 : siguiente);
            if (coste < mejorCoste) {
                mejorCoste = coste;
                mejorObjetivo = candidato;
            }
        }
        if (mejorObjetivo >= 0) {
            MOVE mov = legal(m, m.movimientoPorCamino(m.caminoHasta(mejorObjetivo)));
            if (mov != null) {
                return mov;
            }
        }
        return movimientoSeguro(m, quedanComunes);
    }
}