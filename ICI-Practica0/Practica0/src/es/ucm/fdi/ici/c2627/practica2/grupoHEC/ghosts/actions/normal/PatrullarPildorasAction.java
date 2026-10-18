package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * PATRULLAR PÍLDORAS: ir hacia la píldora más cercana a Ms. Pac-Man, que es donde
 * probablemente vaya a comer. Ocupa esa zona sin ir en fila detrás del perseguidor.
 *
 * Si no quedan píldoras normales, persigue.
 */
public class PatrullarPildorasAction implements Action {

	private final GHOST ghost;

	public PatrullarPildorasAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int pildora = Movimientos.pildoraCercanaPacman(game);
		if (pildora < 0) return Movimientos.irHacia(game, ghost, game.getPacmanCurrentNodeIndex());

		return Movimientos.irHacia(game, ghost, pildora);
	}

	@Override
	public String getActionId() {
		return ghost + " patrullar píldoras";
	}
}