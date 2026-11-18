package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions;

import static es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.GhostsConstants.*;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.utils.Movimientos;
import java.util.HashSet;
import java.util.Set;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * INTERCEPTAR: cortarle el paso a Ms. Pac-Man por delante (Movimientos.intercepcion).
 * Si no hay punto de corte con ventaja suficiente, va flanqueando.
 */
public class InterceptarAction implements Action {

	private final GHOST ghost;

	public InterceptarAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		int pacman = game.getPacmanCurrentNodeIndex();
		int yo = game.getGhostCurrentNodeIndex(ghost);
		Movimientos.Intercepcion corte = Movimientos.intercepcion(game, ghost);
		if (corte.margen >= MARGEN_INTERCEPCION && corte.objetivo != yo && corte.objetivo != pacman)
			return Movimientos.irHacia(game, ghost, corte.objetivo);
		int destino = (corte.objetivo == pacman || corte.objetivo == yo) ? pacman : corte.objetivo;
		return Movimientos.irHaciaFlanqueando(game, ghost, destino, caminosDeLosDemas(game, pacman));
	}

	@Override
	public String getActionId() {
		return "interceptar";
	}

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

	@Override
	public String toString() {
		return getActionId();
	}
}