package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.peligro;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIDA EFICIENTE (en PELIGRO POR PÍLDORA): Ms. Pac-Man está a punto de comerse una power
 * pill, así que el fantasma se aleja de esa pill antes de volverse comestible. Usa la misma
 * huida que COMESTIBLE, pero el peligro es la pill, que no se mueve (ultimoPeligro = null),
 * y el fantasma aún va a velocidad normal.
 */
public class HuirDePPAction implements Action {

	private final GHOST ghost;

	public HuirDePPAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int pill = Movimientos.powerPillCercanaPacman(game);
		if (pill < 0)       // ya no quedan pills: no hay de qué huir, el siguiente turno sale de PELIGRO
			return Movimientos.irHacia(game, ghost, game.getPacmanCurrentNodeIndex());

		return Movimientos.huidaEficiente(game, ghost, pill, null);
	}

	@Override
	public String getActionId() {
		return ghost + " huida de la power pill";
	}
}