package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.Constantes;

/**
 * Ms. Pac-Man llega a la píldora de poder más cercana (a HUIR_PODER_ALCANCE nodos o menos) con más de
 * 'ventaja' nodos de diferencia sobre el fantasma peligroso que más cerca está de ella. Hace falta algún
 * fantasma peligroso: sin él no hay de qué huir.
 */
public class VentajaEnPildoraPoderTransition implements Transition {

    private final int ventaja;

    public VentajaEnPildoraPoderTransition(int ventaja) {
        this.ventaja = ventaja;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        int pildora = m.getNearestPowerPill();
        if (pildora < 0 || m.getNearestDangerous() == null || m.getDistNearestPowerPill() > Constantes.HUIR_PODER_ALCANCE) {
            return false;
        }
        int fantasma = m.distanciaMinimaFantasma(pildora);
        return fantasma - m.getDistNearestPowerPill() > ventaja;
    }

    @Override
    public String toString() {
        return "Ventaja de más de " + ventaja + " en la píldora de poder";
    }
}