package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Parametros;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Ruta;
import pacman.game.Constants.DM;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * rumbo (nueva en la v8): el primer paso del camino más corto a la píldora más cercana, o a la
 * power pill más cercana si ya no quedan normales (sin ellas no se acaba el nivel).
 *
 * Por qué una acción nueva: en los atascos de final de nivel (herramienta Atasco), recolectar
 * e irAPildora elegían el movimiento contrario al del camino más corto con todos los fantasmas
 * a más de 40 pasos: sus rutas miran sólo tres pasillos y castigan los callejones aunque no haya
 * peligro. Ésta no puntúa nada: sigue el camino. Lo único que comprueba es que la ruta que empieza
 * por ese movimiento no sea insegura; si lo es, va hacia el objetivo por la ruta más segura.
 *
 * OJO: en este motor Ms. Pac-Man NO puede dar media vuelta (correctPacManDir ignora la dirección
 * opuesta y sigue recto). Por eso el camino más corto se calcula sin poder invertir: con el
 * camino más corto normal, pedía media vuelta, el motor la ignoraba y se alejaba del objetivo.
 */
public class RumboAction extends AccionPacMan {

    public RumboAction(Contexto contexto) {
        super("rumbo", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        Game game = in.getGame();
        int pac = game.getPacmanCurrentNodeIndex();
        int[] objetivos = in.getPildoras().length > 0 ? in.getPildoras() : game.getActivePowerPillsIndices();
        if (objetivos.length == 0)
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        MOVE ultimo = in.getUltimoMov();
        int objetivo = objetivos[0];
        int mejor = Integer.MAX_VALUE;
        for (int o : objetivos) {          // el más cercano SIN dar media vuelta
            int d = (ultimo == MOVE.NEUTRAL) ? game.getShortestPathDistance(pac, o) : game.getShortestPathDistance(pac, o, ultimo);
            if (d < mejor) { mejor = d; objetivo = o; }
        }
        MOVE m = (ultimo == MOVE.NEUTRAL) ? game.getNextMoveTowardsTarget(pac, objetivo, DM.PATH)
                : game.getNextMoveTowardsTarget(pac, objetivo, ultimo, DM.PATH);
        for (Ruta r : in.getRutas())
            if (r.getMovimiento() == m && r.getMargen() <= Parametros.MARGEN_SEGURO)
                return Navegacion.irA(in, objetivo, true);     // por ahí no: lo más seguro hacia el objetivo
        return m;
    }
}
