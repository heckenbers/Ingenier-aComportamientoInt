package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * PERSEGUIR: ir hacia la posición de Ms. Pac-Man por el camino más corto, sin darse la vuelta.
 * Solo decide en los cruces; en un pasillo el motor sigue recto.
 */
public class PerseguirAction implements Action {

	private final GHOST ghost;

	public PerseguirAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		return Movimientos.irHacia(game, ghost, game.getPacmanCurrentNodeIndex());
	}

	@Override
	public String getActionId() {
		return ghost + " perseguir";
	}
}