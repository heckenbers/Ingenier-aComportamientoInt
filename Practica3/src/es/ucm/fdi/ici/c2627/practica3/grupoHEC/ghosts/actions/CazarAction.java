package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import java.util.HashSet;
import java.util.Set;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * CAZAR: ir a por Ms. Pac-Man por otro camino: flanquea evitando los caminos de los demás
 * fantasmas hasta ella, para cerrarle otra salida en vez de ir en fila.
 * Si no hay ningún camino que flanquee, va por el más corto.
 */
public class CazarAction implements RulesAction {

	private final GHOST ghost;

	public CazarAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int pacman = game.getPacmanCurrentNodeIndex();
		return Movimientos.irHaciaFlanqueando(game, ghost, pacman, caminosDeLosDemas(game, pacman));
	}

	@Override
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "cazar";
	}

	/** Ms. Pac-Man y los caminos hasta ella de los demás fantasmas que van a por ella (no comestibles). */
	private Set<Integer> caminosDeLosDemas(Game game, int pacman) {
		Set<Integer> evitar = new HashSet<>();
		evitar.add(pacman);
		for (GHOST otro : GHOST.values()) {
			if (otro == ghost || game.getGhostLairTime(otro) > 0 || game.isGhostEdible(otro)) continue;
			int nodo = game.getGhostCurrentNodeIndex(otro);
			evitar.add(nodo);
			for (int n : game.getShortestPath(nodo, pacman, game.getGhostLastMoveMade(otro)))
				evitar.add(n);
		}
		return evitar;
	}
}