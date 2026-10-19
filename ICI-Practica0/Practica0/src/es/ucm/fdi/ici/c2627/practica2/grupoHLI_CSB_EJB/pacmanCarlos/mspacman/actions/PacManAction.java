package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Base de las acciones de Pac-Man. Implementa Action (así se pueden usar
 * también con SimpleState) y añade execute(MsPacManInput), que es la que
 * llama EstadoConInput con las percepciones ya calculadas.
 */
public abstract class PacManAction implements Action {

    private final String id;

    protected PacManAction(String id) {
        this.id = id;
    }

    @Override
    public String getActionId() {
        return id;
    }

    /** Compatibilidad con SimpleState: calcula el input y delega. */
    @Override
    public MOVE execute(Game game) {
        return execute(new MsPacManInput(game));
    }

    public abstract MOVE execute(MsPacManInput in);
}
