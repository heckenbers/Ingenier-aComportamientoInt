package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * CERRAR PASO: Ms. Pac-Man está en un pasillo y no puede darse la vuelta, así que tiene que
 * salir por el cruce del final. Este fantasma llega a ese cruce antes que ella (lo comprueba
 * el reparto) y entra en el pasillo de frente: queda atrapada.
 *  1. Mientras no ha llegado al cruce, va hacia él.
 *  2. En el cruce, o ya dentro del pasillo, va directo hacia ella.
 * Si ella ya no está en un pasillo (el reparto le quitará el papel en el turno siguiente), va a por ella.
 */
public class CerrarPasoAction implements Action {

	private final GHOST ghost;

	public CerrarPasoAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int pacman = game.getPacmanCurrentNodeIndex();
		Movimientos.Tramo pasillo = Movimientos.tramoPacman(game);
		if (pasillo == null) return Movimientos.irHacia(game, ghost, pacman);

		int yo = game.getGhostCurrentNodeIndex(ghost);
		if (yo == pasillo.cruce || pasillo.nodos.contains(yo))
			return Movimientos.irHacia(game, ghost, pacman);       // ya está delante de ella: de frente

		return Movimientos.irHacia(game, ghost, pasillo.cruce);
	}

	@Override
	public String getActionId() {
		return ghost + " cerrar paso";
	}
}