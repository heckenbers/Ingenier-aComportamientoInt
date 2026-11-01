package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import java.util.HashSet;
import java.util.Set;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * INTERCEPTAR: cortarle el paso a Ms. Pac-Man por delante (Movimientos.intercepcion): el primer
 * nodo de su camino (hacia la PP si está cerca de una, si no hacia su siguiente cruce) al que
 * llega antes que ella. Si hay punto de corte, va por el camino más corto (con el que se calculó).
 * Si no, va flanqueando: sin pasar por ella ni por los caminos de los demás fantasmas hasta ella.
 */
public class InterceptarAction implements RulesAction {

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

		if (corte.margen >= GhostsConstants.MARGEN_INTERCEPCION && corte.objetivo != yo && corte.objetivo != pacman)
			return Movimientos.irHacia(game, ghost, corte.objetivo);

		int destino = (corte.objetivo == pacman || corte.objetivo == yo) ? pacman : corte.objetivo;
		return Movimientos.irHaciaFlanqueando(game, ghost, destino, caminosDeLosDemas(game, pacman));
	}

	@Override
	public void parseFact(Fact actionFact) { }

	@Override
	public String getActionId() {
		return "interceptar";
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