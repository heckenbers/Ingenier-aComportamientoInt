package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * HUIR DE LA PP: Ms. Pac-Man está a punto de comerse una power pill y el fantasma (aún no
 * comestible) se aparta de ella. Elige la salida teniendo en cuenta que, al comérsela, el
 * motor invierte el sentido de todos los fantasmas (Movimientos.huidaAnticipandoInversion).
 */
public class HuirDePPAction implements RulesAction {

	private final GHOST ghost;

	public HuirDePPAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int pp = Movimientos.powerPillCercanaPacman(game);
		if (pp < 0) return Movimientos.irHacia(game, ghost, game.getPacmanCurrentNodeIndex());

		int ticksPacman = Movimientos.distanciaPacman(game, pp);     // cuándo se invierten todos
		return Movimientos.huidaAnticipandoInversion(game, ghost, pp, ticksPacman);
	}

	@Override
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "huirDePP";
	}
}