package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.peligro;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * BLOQUEAR POWER PILL: el fantasma llega a la pill amenazada antes que Ms. Pac-Man (*III)
 * y se pone en medio.
 *  1. Mientras está lejos, va hacia la pill.
 *  2. Cuando ya la guarda (a RADIO_GUARDIA_PP o menos), sale al encuentro de Ms. Pac-Man.
 *     No puede quedarse quieto encima de la pill, y si se limitara a ir hacia ella daría
 *     vueltas alejándose. Yendo hacia ella, le corta el paso por el camino por el que viene:
 *     o la come o la obliga a darse la vuelta.
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
		int pill = Movimientos.powerPillCercanaPacman(game);
		if (pill < 0)       // ya no quedan pills: perseguir, el siguiente turno sale de PELIGRO
			return Movimientos.irHacia(game, ghost, pacman);

		int aLaPill = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), pill);
		if (aLaPill <= GhostsConstants.RADIO_GUARDIA_PP)
			return Movimientos.irHacia(game, ghost, pacman);    // ya la guarda: cortarle el paso

		return Movimientos.irHacia(game, ghost, pill);
	}

	@Override
	public String getActionId() {
		return ghost + " bloquear power pill";
	}
}