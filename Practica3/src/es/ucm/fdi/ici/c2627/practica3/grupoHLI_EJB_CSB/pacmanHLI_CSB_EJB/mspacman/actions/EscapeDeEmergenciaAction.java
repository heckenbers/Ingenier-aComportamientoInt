package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Turno;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Parametros;
import pacman.game.Constants.MOVE;
import static pacman.game.Constants.EAT_DISTANCE;

/**
 * SUPERVIVENCIA / EscapeDeEmergencia. Pac-Man está acorralado o casi.
 * Prioridades:
 *  1. una píldora de poder a la que llega antes que cualquier fantasma;
 *  2. un túnel con margen;
 *  3. la ruta con más margen y que acabe en el nodo más abierto.
 * No filtra por seguridad: en emergencia se elige lo menos malo.
 */
public class EscapeDeEmergenciaAction extends PacManAction {

    public EscapeDeEmergenciaAction(Turno turno) {
        super("EscapeDeEmergencia", turno);
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        MapaNivel mapa = in.getMapa();
        return Navegacion.elegirSinFiltro(in, r -> {
            double p = 0;
            if (r.getPosPoder() >= 0 && r.getMargenCorredor() > EAT_DISTANCE)
                p += 10000 - r.getPosPoder();
            if (r.isTunel() && r.getMargen() > EAT_DISTANCE)
                p += 300;
            p += 100.0 * Navegacion.seguridad(r);
            p += 5.0 * mapa.getApertura(r.getFin());
            if (r.isColgante())
                p -= Parametros.PENAL_COLGANTE;
            return p;
        });
    }
}
