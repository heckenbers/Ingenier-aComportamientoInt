package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import pacman.game.Constants.GHOST;

/**
 * Ruta interceptada: un fantasma no comestible corta el camino hacia la píldora de poder y está a menos
 * de 'limite' nodos de ella. Solo tiene sentido dentro de PODER, desde ir a la píldora.
 */
public class RutaPoderInterceptadaTransition implements Transition {

    private final int limite;

    public RutaPoderInterceptadaTransition(int limite) {
        this.limite = limite;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        int pildora = m.getNearestPowerPill();
        if (pildora < 0) {
            return false;
        }
        int[] camino = m.caminoHasta(pildora);
        for (GHOST g : m.getDangerousGhosts()) {
            int d = m.distanciaDeFantasma(g, pildora);
            if (d >= 0 && d < limite && m.fantasmaIntercepta(g, camino)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Ruta a la píldora de poder cortada a < " + limite;
    }
}