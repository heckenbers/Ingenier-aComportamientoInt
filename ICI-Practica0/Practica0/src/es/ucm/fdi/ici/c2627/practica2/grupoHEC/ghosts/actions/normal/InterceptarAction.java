package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.normal;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto.Papel;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * INTERCEPTAR: cortarle el paso a Ms. Pac-Man por delante (Movimientos.intercepcion): el primer
 * nodo de su camino al que el fantasma llega antes que ella. Si ella está cerca de una power
 * pill, ese camino es el que lleva a la PP: así le corta el paso hacia ella. Si no, el de su
 * siguiente cruce.
 *
 * Si hay punto de corte, va a él por el camino más corto (es con el que se calculó que
 * llega antes). Si no lo hay, va flanqueando: por un camino que no comparta nodos con el del
 * perseguidor ni pase por ella, aunque sea más largo, para no ir los dos en fila.
 *
 * Si el objetivo es ella misma o el fantasma ya está en él, va a por ella, también
 * flanqueando. Si no hay ningún camino que flanquee, usa el más corto.
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
		int yo = game.getGhostCurrentNodeIndex(ghost);
		Movimientos.Intercepcion corte = Movimientos.intercepcion(game, ghost);

		// Hay un punto de su camino al que llega antes que ella: ir por el camino más corto.
		// Ese punto se calculó con la distancia más corta; flanqueando (camino más largo)
		// podría llegar tarde. Además, si llega antes, ya la está cortando de frente.
		if (corte.margen >= GhostsConstants.MARGEN_INTERCEPCION && corte.objetivo != yo && corte.objetivo != pacman)
			return Movimientos.irHacia(game, ghost, corte.objetivo);

		// Si no puede adelantarse: hacia su siguiente cruce (o hacia ella), flanqueando al perseguidor
		int destino = corte.objetivo;
		if (destino == pacman || destino == yo)
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