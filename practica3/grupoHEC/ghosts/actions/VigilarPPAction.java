package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * VIGILAR PP: colocarse junto a la power pill más cercana a Ms. Pac-Man, para estar cerca
 * cuando ella la amenace. Si no quedan power pills, persigue.
 */
public class VigilarPPAction implements RulesAction {

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
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "vigilarPP";
	}
}