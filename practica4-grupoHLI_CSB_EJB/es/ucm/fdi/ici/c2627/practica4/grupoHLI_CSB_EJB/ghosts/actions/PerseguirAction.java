package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions;

import es.ucm.fdi.ici.Action;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class PerseguirAction implements Action {

	private final GHOST yo;

	public PerseguirAction(GHOST yo) {
		this.yo = yo;
	}

	@Override
	public MOVE execute(Game game) {
		return game.getApproximateNextMoveTowardsTarget(
				game.getGhostCurrentNodeIndex(yo),
				game.getPacmanCurrentNodeIndex(),
				game.getGhostLastMoveMade(yo),
				DM.PATH);
	}

	@Override
	public String getActionId() {
		return "perseguir";
	}

	@Override
	public String toString() {
		return getActionId();
	}
}