package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * ENCERRADO: el fantasma está en la cárcel esperando a salir. No hay nada que decidir
 * (doesGhostRequireAction es false), así que devuelve NEUTRAL.
 */
public class EncerradoAction implements Action {

	private final GHOST ghost;

	public EncerradoAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		return MOVE.NEUTRAL;
	}

	@Override
	public String getActionId() {
		return ghost + " encerrado";
	}
}