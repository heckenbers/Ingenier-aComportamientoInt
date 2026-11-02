package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Base de las acciones de Pac-Man (Práctica 3: ahora son RulesAction).
 *
 * El RuleEngine llama a parseFact(hecho ACTION ganador) y después a
 * execute(Game). execute recoge el MsPacManInput que el controlador ya
 * calculó en este turno (Turno) y delega en execute(MsPacManInput), que es
 * el mismo método que en la Práctica 2.
 */
public abstract class PacManAction implements RulesAction {

    private final String id;
    private final Turno turno;

    protected PacManAction(String id, Turno turno) {
        this.id = id;
        this.turno = turno;
    }

    @Override
    public String getActionId() {
        return id;
    }

    /** Por defecto la acción no tiene parámetros. */
    @Override
    public void parseFact(Fact actionFact) {
    }

    @Override
    public MOVE execute(Game game) {
        MsPacManInput in = turno.get(game);
        if (in == null)
            return MOVE.NEUTRAL;                // no debería pasar: el controlador siempre lo pone
        MOVE m = execute(in);
        return (m == null) ? MOVE.NEUTRAL : m;
    }

    public abstract MOVE execute(MsPacManInput in);
}
