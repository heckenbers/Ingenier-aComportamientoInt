package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIR DE LA PP: Ms. Pac-Man está a punto de comerse una power pill y el fantasma (aún no
 * comestible) se aparta de ella teniendo en cuenta la inversión de sentido del motor.
 */
public class HuirDePPAction implements Action {

	private final GHOST ghost;

	public HuirDePPAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int pp = Movimientos.powerPillCercanaPacman(game);
		if (pp < 0) return Movimientos.irHacia(game, ghost, game.getPacmanCurrentNodeIndex());
		int ticksPacman = Movimientos.distanciaPacman(game, pp);
		return Movimientos.huidaAnticipandoInversion(game, ghost, pp, ticksPacman);
	}

	@Override
	public String getActionId() {
		return "huirDePP";
	}

	@Override
	public String toString() {
		return getActionId();
	}
}