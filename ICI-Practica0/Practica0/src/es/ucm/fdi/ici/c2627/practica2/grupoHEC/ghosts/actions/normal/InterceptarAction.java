package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto.Papel;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * INTERCEPTAR: ir al cruce hacia el que se dirige Ms. Pac-Man (Movimientos.proximoCrucePacman),
 * flanqueando: por un camino que no comparta nodos con el del perseguidor ni pase por ella,
 * aunque sea más largo. Así el perseguidor empuja por detrás y el interceptor le corta el paso
 * por delante, en vez de ir los dos en fila por el mismo pasillo.
 *
 * Si no se puede prever el cruce (ella está en uno o parada), o el fantasma ya está en él,
 * va a por ella, también flanqueando. Si no hay ningún camino que flanquee, usa el más corto.
 */
public class InterceptarAction implements Action {

	private final GHOST ghost;
	private final Supplier<Reparto> reparto;

	public InterceptarAction(GHOST ghost, Supplier<Reparto> reparto) {
		this.ghost = ghost;
		this.reparto = reparto;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int pacman = game.getPacmanCurrentNodeIndex();
		int destino = Movimientos.proximoCrucePacman(game);
		if (destino == pacman || destino == game.getGhostCurrentNodeIndex(ghost))
			destino = pacman;

		return Movimientos.irHaciaFlanqueando(game, ghost, destino, nodosAEvitar(game, pacman));
	}

	/** Ms. Pac-Man y el camino del perseguidor hasta ella: por ahí ya va él. */
	private Set<Integer> nodosAEvitar(Game game, int pacman) {
		Set<Integer> evitar = new HashSet<>();
		evitar.add(pacman);

		GHOST perseguidor = reparto.get().quienTiene(Papel.PERSEGUIR);
		if (perseguidor != null && perseguidor != ghost && game.getGhostLairTime(perseguidor) <= 0) {
			int nodo = game.getGhostCurrentNodeIndex(perseguidor);
			evitar.add(nodo);
			for (int n : game.getShortestPath(nodo, pacman, game.getGhostLastMoveMade(perseguidor)))
				evitar.add(n);
		}
		return evitar;
	}

	@Override
	public String getActionId() {
		return ghost + " interceptar";
	}
}