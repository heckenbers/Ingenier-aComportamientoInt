package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * VIGILAR PP: colocarse junto a la power pill más cercana a Ms. Pac-Man antes de que ella
 * la amenace. Así hay un fantasma cerca que podrá bloquearla (PELIGRO POR PÍLDORA) si
 * ella se acerca. Al llegar, como no puede pararse ni dar la vuelta, da vueltas cerca de ella.
 *
 * Si ya no quedan power pills (el reparto no debería darle este papel), persigue.
 */
public class VigilarPPAction implements Action {

	private final GHOST ghost;

	public VigilarPPAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int pp = Movimientos.powerPillCercanaPacman(game);
		if (pp < 0) return Movimientos.irHacia(game, ghost, game.getPacmanCurrentNodeIndex());

		return Movimientos.irHacia(game, ghost, pp);
	}

	@Override
	public String getActionId() {
		return ghost + " vigilar PP";
	}
}