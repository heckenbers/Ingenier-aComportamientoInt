package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.states;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.State;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.PacManAction;
import pacman.game.Constants.MOVE;

/**
 * Estado hoja. Igual que SimpleState (id + acción), pero le pasa a la acción
 * el MsPacManInput ya calculado en este tick en vez de solo el Game, para no
 * recalcular las percepciones en cada acción.
 */
public class EstadoConInput implements State {

    private final PacManAction action;
    public final String id;

    public EstadoConInput(PacManAction action) {
        this(action.getActionId(), action);
    }

    public EstadoConInput(String id, PacManAction action) {
        this.id = id;
        this.action = action;
    }

    @Override
    public MOVE execute(Input input) {
        return action.execute((MsPacManInput) input);
    }

    @Override
    public void stop() {
        // sin acción de salida
    }

    @Override
    public String toString() {
        return id;
    }
}
