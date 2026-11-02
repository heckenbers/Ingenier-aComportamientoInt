package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Turno;
import jess.Fact;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;

/**
 * RECOLECTAR / IrAPíldoraRentable (subestado inicial). Acción con parámetro:
 *
 *   (modo normal)  la de la Práctica 2: la ruta segura que más píldoras recoge,
 *                  priorizando clústeres grandes y cercanos, sin pasar por
 *                  píldoras de poder (se guardan para el cebo) ni meterse en
 *                  callejones. Es el valor por defecto, y el único que usan
 *                  v0, v1 y v2 (su plantilla ACTION ni siquiera tiene el slot).
 *
 *   (modo final)   v3, al final del nivel: la ruta segura más corta hasta la
 *                  píldora más cercana, sin penalizar píldoras de poder ni
 *                  callejones. Al final del nivel las píldoras que quedan suelen
 *                  estar justo ahí, y las penalizaciones del modo normal (500 y
 *                  200) pesaban más que la atracción hacia ellas (≈ 27 a 10
 *                  casillas): Ms. Pac-Man daba vueltas sin ir a por ellas.
 *                  La seguridad no depende de esas penalizaciones: elegir()
 *                  sigue descartando las rutas sin margen.
 */
public class IrAPildoraRentableAction extends PacManAction {

    public static final String SLOT_MODO = "modo";
    private boolean modoFinal;

    public IrAPildoraRentableAction(Turno turno) {
        super("IrAPildoraRentable", turno);
    }

    @Override
    public void parseFact(Fact actionFact) {
        modoFinal = false;
        try {
            modoFinal = "final".equals(actionFact.getSlotValue(SLOT_MODO).symbolValue(null));
        } catch (Exception e) {
            // sin slot modo (v0, v1, v2): modo normal
        }
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        if (!modoFinal)
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);

        int[] objetivos = in.getPildoras();
        if (objetivos.length == 0)
            objetivos = in.getPildorasPoder();
        if (objetivos.length == 0)
            return Navegacion.masSegura(in);
        int pacman = in.getPacman();
        int destino = in.getGame().getClosestNodeIndexFromNodeIndex(pacman, objetivos, DM.PATH);
        return Navegacion.irA(in, destino, true);
    }
}
