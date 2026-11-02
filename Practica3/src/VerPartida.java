import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.Ghosts;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.MsPacMan;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.depurar.TrazaReglas;
import es.ucm.fdi.ici.rivales.MsPacManAvanzado;
import pacman.Executor;
import pacman.game.Constants.GHOST;

/**
 * Una partida en pantalla, con la traza de las reglas en la consola.
 *   VEO_FANTASMAS = true  -> nuestros fantasmas contra una Ms. Pac-Man de referencia,
 *                            con la traza de los cuatro fantasmas (o solo FANTASMA_TRAZA)
 *   VEO_FANTASMAS = false -> nuestra Ms. Pac-Man contra GhostsAggressive, con su traza
 */
public class VerPartida {

    static final boolean VEO_FANTASMAS = true;
    static final String VERSION_FANTASMAS = "ghosts-v5.clp";
    static final boolean TRAZO_LOS_CUATRO = true;        // false: solo FANTASMA_TRAZA
    static final GHOST FANTASMA_TRAZA = GHOST.PINKY;

    public static void main(String[] args) {
        Executor executor = new Executor.Builder()
                .setTickLimit(4000)
                .setVisual(true)
                .setScaleFactor(3.0)
                .build();

        List<TrazaReglas> trazas = new ArrayList<>();
        if (VEO_FANTASMAS) {
            Ghosts ghosts = new Ghosts(VERSION_FANTASMAS);
            // Algunas reglas (vigilar, bloquear, cerrar paso...) solo las dispara UN fantasma cada
            // vez: con la traza de uno solo pueden salir NUNCA aunque los demás sí las disparen
            for (GHOST g : GHOST.values())
                if (TRAZO_LOS_CUATRO || g == FANTASMA_TRAZA)
                    trazas.add(new TrazaReglas(g.name(), ghosts.getMotor(g), true));   // true: sólo los cambios
            executor.runGame(new MsPacManAvanzado(), ghosts, 40);   // la que más PP come: así hay comestibles
        } else {
            MsPacMan pacman = new MsPacMan();
            trazas.add(new TrazaReglas("MSPACMAN", pacman.getMotor(), true));
            executor.runGame(pacman, new GhostsAggressive(), 40);
        }
        for (TrazaReglas t : trazas) t.resumen();
    }
}