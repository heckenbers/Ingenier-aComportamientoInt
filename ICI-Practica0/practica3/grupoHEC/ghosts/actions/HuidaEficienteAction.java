package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIDA EFICIENTE (comestible): alejarse de Ms. Pac-Man eligiendo bien el pasillo, mirando dos
 * cruces por delante y teniendo en cuenta lo que le queda de comestible (Movimientos.huidaEficiente).
 */
public class HuidaEficienteAction implements RulesAction {

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
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "huidaEficiente";
	}
}