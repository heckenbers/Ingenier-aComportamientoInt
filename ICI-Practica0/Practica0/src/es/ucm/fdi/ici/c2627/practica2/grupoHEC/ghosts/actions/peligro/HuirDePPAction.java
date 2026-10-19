package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.peligro;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIDA EFICIENTE (en PELIGRO POR PÍLDORA): Ms. Pac-Man está a punto de comerse una power
 * pill, así que el fantasma se aparta de esa pill antes de volverse comestible.
 *
 * Tiene en cuenta que, al comérsela ella, el motor invierte a todos los fantasmas: elige la
 * salida según dónde le dejará esa inversión (Movimientos.huidaAnticipandoInversion), no
 * solo según lo que se aleja. Huir alejándose sin más acababa, tras la inversión, en ir hacia ella.
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

		// Turnos que tarda Ms. Pac-Man en comérsela: en ese turno se invierten todos los fantasmas
		int ticksPacman = Movimientos.distanciaPacman(game, pill);
		return Movimientos.huidaAnticipandoInversion(game, ghost, pill, ticksPacman);
	}

	@Override
	public String getActionId() {
		return ghost + " huida de la power pill";
	}
}