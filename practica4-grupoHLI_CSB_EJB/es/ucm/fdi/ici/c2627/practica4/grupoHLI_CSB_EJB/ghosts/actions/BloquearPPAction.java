package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions;

import static es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.GhostsConstants.*;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * BLOQUEAR PP: el fantasma llega a la power pill amenazada antes que Ms. Pac-Man y se pone en
 * medio. Mientras está lejos va hacia la PP; cuando ya la guarda (RADIO_GUARDIA_PP) sale al
 * encuentro de Ms. Pac-Man por el camino por el que viene: la come o la obliga a girar.
 */
public class BloquearPPAction implements Action {

	private final GHOST ghost;

	public BloquearPPAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int pacman = game.getPacmanCurrentNodeIndex();
		int pp = Movimientos.powerPillCercanaPacman(game);
		if (pp < 0) return Movimientos.irHacia(game, ghost, pacman);
		int aLaPP = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pp);
		if (aLaPP <= RADIO_GUARDIA_PP) return Movimientos.irHacia(game, ghost, pacman);
		return Movimientos.irHacia(game, ghost, pp);
	}

	@Override
	public String getActionId() {
		return "bloquearPP";
	}

	@Override
	public String toString() {
		return getActionId();
	}
}