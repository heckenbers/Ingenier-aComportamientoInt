package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Parametros;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Turno;
import jess.Fact;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * RECOLECTAR / PrepararCebo (v1). Acción con parámetro:
 *   (ACTION (id PrepararCebo) (modo patrullar) ...)  patrulla a RADIO_PATRULLA
 *       de la píldora de poder sin comérsela mientras los fantasmas se acercan;
 *   (ACTION (id PrepararCebo) (modo comer) ...)      va a comérsela.
 *
 * En la Práctica 2 esta acción decidía ella misma cuándo comerse el cebo
 * (UMBRAL_CADENA, PACIENCIA_CEBO) y apuntaba la espera en MemoriaPacMan. Ahora
 * lo deciden las reglas comerCeboYa / comerCeboPorPaciencia, y la espera la
 * cuenta MemoriaCebo. Aquí solo queda el movimiento.
 */
public class PrepararCeboAction extends PacManAction {

    public static final String SLOT_MODO = "modo";
    private boolean comer;

    public PrepararCeboAction(Turno turno) {
        super("PrepararCebo", turno);
    }

    @Override
    public void parseFact(Fact actionFact) {
        comer = false;
        try {
            comer = "comer".equals(actionFact.getSlotValue(SLOT_MODO).symbolValue(null));
        } catch (Exception e) {
            // sin slot modo: patrullar
        }
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        Game game = in.getGame();
        int p = in.getPildoraCebo();
        if (p < 0)                              // sin cebo: las reglas lo abandonan el turno siguiente
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        if (comer)
            return Navegacion.irA(in, p, true);
        // patrulla: quedarse a RADIO_PATRULLA de p, comiendo lo que pille por el camino
        return Navegacion.elegir(in, r -> {
            int d = game.getShortestPathDistance(r.getFin(), p);
            return -5.0 * Math.abs(d - Parametros.RADIO_PATRULLA)
                    + 2.0 * r.getPildoras()
                    + Math.min(r.getMargen(), 30)
                    - (r.getPosPoder() >= 0 ? Parametros.PENAL_PODER : 0)
                    - (r.isColgante() ? Parametros.PENAL_COLGANTE : 0);
        });
    }
}
