package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.comestible;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIDA EFICIENTE (comestible): alejarse de Ms. Pac-Man eligiendo bien el pasillo
 * (Movimientos.huidaEficiente): nunca por donde viene ella, mejor por pasillos sin otros
 * comestibles y hacia el cruce donde más margen se le saca, teniendo en cuenta que un
 * comestible va a media velocidad.
 */
public class HuidaEficienteAction implements Action {

	private final GHOST ghost;

	public HuidaEficienteAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		return Movimientos.huidaEficiente(game, ghost,
				game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
	}

	@Override
	public String getActionId() {
		return ghost + " huida eficiente";
	}
}