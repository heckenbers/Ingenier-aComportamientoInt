package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Base de las acciones: reutilizan la percepción que el controlador ya ha calculado en
 * este tick (está en el Contexto), en vez de recalcularla. Su id es el nombre de su
 * VAR_OUTPUT en el FCL, y toString() lo devuelve para la traza.
 */
public abstract class AccionPacMan implements Action {

    private final String id;
    protected final Contexto contexto;

    protected AccionPacMan(String id, Contexto contexto) {
        this.id = id;
        this.contexto = contexto;
    }

    @Override
    public MOVE execute(Game game) {
        MOVE m = execute(contexto.getPercepcion());
        return m == null ? MOVE.NEUTRAL : m;
    }

    protected abstract MOVE execute(Percepcion in);

    @Override
    public String getActionId() { return id; }

    @Override
    public String toString() { return id; }
}
