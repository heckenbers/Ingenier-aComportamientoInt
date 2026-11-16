package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;

/**
 * Cambia de trayectoria hacia un cruce alternativo: de los cruces, los que menos rodeo suponen para llegar a la
 * píldora de poder (Ms. Pac-Man hasta el cruce más el cruce hasta la píldora), y se queda con el primero cuya
 * ruta no corte ningún fantasma.
 */
public class EvitarCaminoAction extends MsPacManAction {

    public EvitarCaminoAction() {
        super("EvitarCamino");
    }

    @Override
    protected MOVE decide(MsPacManInput m) {
        int pildora = m.getNearestPowerPill();
        if (pildora < 0) {
            return movimientoSeguro(m, false);
        }
        int[] cruces = m.getGame().getJunctionIndices();
        int[] candidatos = new int[cruces.length];
        int[] rodeo = new int[cruces.length];
        int n = 0;
        for (int cruce : cruces) {
            if (cruce == m.getPacman()) {
                continue;
            }
            int hastaCruce = m.distanciaPacman(cruce);
            int desdeCruce = m.distanciaEntre(cruce, pildora);
            if (hastaCruce < 0 || desdeCruce < 0) {
                continue;
            }
            candidatos[n] = cruce;
            rodeo[n] = hastaCruce + desdeCruce;
            n++;
        }
        // Selección parcial: se sacan de uno en uno los de menor rodeo
        for (int r = 0; r < Constantes.NUM_CANDIDATOS_DESVIO && r < n; r++) {
            int mejor = r;
            for (int i = r + 1; i < n; i++) {
                if (rodeo[i] < rodeo[mejor]) {
                    mejor = i;
                }
            }
            int tmpNodo = candidatos[r];
            candidatos[r] = candidatos[mejor];
            candidatos[mejor] = tmpNodo;
            int tmpRodeo = rodeo[r];
            rodeo[r] = rodeo[mejor];
            rodeo[mejor] = tmpRodeo;

            int[] camino = m.caminoHasta(candidatos[r]);
            if (camino.length == 0 || m.rutaInterceptada(camino)) {
                continue;
            }
            MOVE mov = legal(m, m.movimientoPorCamino(camino));
            if (mov != null) {
                return mov;
            }
        }
        return movimientoSeguro(m, false);
    }
}