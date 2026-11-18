package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIDA EFICIENTE (comestible): alejarse de Ms. Pac-Man eligiendo bien el pasillo, mirando dos
 * cruces por delante y teniendo en cuenta lo que le queda de comestible.
 */
public class HuidaEficienteAction implements Action {

	private final GHOST ghost;

	public HuidaEficienteAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		return Movimientos.huidaEficiente(game, ghost, game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
	}

	@Override
	public String getActionId() {
		return "huidaEficiente";
	}

	@Override
	public String toString() {
		return getActionId();
	}
}