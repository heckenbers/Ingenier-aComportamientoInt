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
 * CAZAR (sustituye a PATRULLAR PÍLDORAS): ir también a por Ms. Pac-Man, pero por un tercer
 * camino. Flanquea como el interceptor, evitando los caminos del perseguidor y del interceptor
 * hasta ella, para cerrarle otra salida en vez de sumarse a la fila.
 * Si no hay ningún camino que flanquee, va por el más corto.
 */
public class CazarAction implements Action {

	private final GHOST ghost;
	private final Supplier<Reparto> reparto;

	public CazarAction(GHOST ghost, Supplier<Reparto> reparto) {
		this.ghost = ghost;
		this.reparto = reparto;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int pacman = game.getPacmanCurrentNodeIndex();
		Set<Integer> evitar = new HashSet<>();
		evitar.add(pacman);
		for (Papel p : new Papel[] { Papel.PERSEGUIR, Papel.INTERCEPTAR })
			anadirCamino(game, reparto.get().quienTiene(p), pacman, evitar);

		return Movimientos.irHaciaFlanqueando(game, ghost, pacman, evitar);
	}

	/** Añade a 'evitar' el nodo de 'otro' y su camino más corto hasta Ms. Pac-Man. */
	private void anadirCamino(Game game, GHOST otro, int pacman, Set<Integer> evitar) {
		if (otro == null || otro == ghost || game.getGhostLairTime(otro) > 0) return;
		int nodo = game.getGhostCurrentNodeIndex(otro);
		evitar.add(nodo);
		for (int n : game.getShortestPath(nodo, pacman, game.getGhostLastMoveMade(otro)))
			evitar.add(n);
	}

	@Override
	public String getActionId() {
		return ghost + " cazar";
	}
}