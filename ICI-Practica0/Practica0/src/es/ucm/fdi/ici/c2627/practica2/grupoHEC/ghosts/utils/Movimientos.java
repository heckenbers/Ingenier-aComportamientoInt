package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Funciones de movimiento y distancia que comparten las acciones de los fantasmas.
 * Todas las distancias se miden por el laberinto y con el último movimiento de quien
 * se mueve, porque ni los fantasmas ni Ms. Pac-Man pueden darse la vuelta.
 * Solo tiene métodos estáticos: no se instancia.
 */
public final class Movimientos {

	private Movimientos() {}

	/** Valor que se devuelve como distancia cuando no tiene sentido medirla (fantasma en la cárcel). */
	public static final int INALCANZABLE = Integer.MAX_VALUE;

	// ---- Tramo: el pasillo entre un cruce y el siguiente ------------------------

	/** Resultado de recorrer un pasillo hasta el siguiente cruce. */
	public static final class Tramo {
		public final MOVE salida;           // movimiento con el que se entra en el pasillo
		public final int cruce;             // cruce donde termina
		public final MOVE llegada;          // movimiento con el que se llega a ese cruce
		public final int pasos;             // nodos recorridos, cruce incluido
		public final List<Integer> nodos;   // los nodos del tramo, en orden

		Tramo(MOVE salida, int cruce, MOVE llegada, int pasos, List<Integer> nodos) {
			this.salida = salida;
			this.cruce = cruce;
			this.llegada = llegada;
			this.pasos = pasos;
			this.nodos = nodos;
		}
	}

	/**
	 * Recorre el pasillo que empieza saliendo de 'desde' por 'salida' hasta el siguiente
	 * cruce, incluido. En un pasillo solo hay una salida que no sea volver atrás, así que
	 * las esquinas se resuelven solas.
	 */
	public static Tramo siguienteCruce(Game game, int desde, MOVE salida) {
		List<Integer> nodos = new ArrayList<>();
		int nodo = game.getNeighbour(desde, salida);
		MOVE dir = salida;
		int max = game.getNumberOfNodes();          // salvaguarda contra bucles

		while (nodo >= 0 && nodos.size() < max) {
			nodos.add(nodo);
			if (game.isJunction(nodo)) break;
			MOVE[] siguientes = game.getPossibleMoves(nodo, dir);
			if (siguientes.length == 0) break;
			dir = siguientes[0];
			nodo = game.getNeighbour(nodo, dir);
		}
		int fin = nodos.isEmpty() ? desde : nodos.get(nodos.size() - 1);
		return new Tramo(salida, fin, dir, nodos.size(), nodos);
	}

	// ---- Distancias -------------------------------------------------------------

	/** Distancia del fantasma a 'destino', sin darse la vuelta. INALCANZABLE si está en la cárcel. */
	public static int distanciaFantasma(Game game, GHOST ghost, int destino) {
		if (game.getGhostLairTime(ghost) > 0 || destino < 0) return INALCANZABLE;
		return game.getShortestPathDistance(game.getGhostCurrentNodeIndex(ghost), destino,
				game.getGhostLastMoveMade(ghost));
	}

	/** Distancia de Ms. Pac-Man a 'destino', sin darse la vuelta. */
	public static int distanciaPacman(Game game, int destino) {
		if (destino < 0) return INALCANZABLE;
		return game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), destino,
				game.getPacmanLastMoveMade());
	}

	/**
	 * De 'objetivos', el más cercano a 'origen' por el laberinto, saliendo con 'ultimo'.
	 * Devuelve -1 si la lista está vacía.
	 */
	public static int masCercano(Game game, int origen, MOVE ultimo, int[] objetivos) {
		int mejor = -1;
		int min = INALCANZABLE;
		for (int o : objetivos) {
			int d = game.getShortestPathDistance(origen, o, ultimo);
			if (d < min) {
				min = d;
				mejor = o;
			}
		}
		return mejor;
	}

	// ---- Movimientos básicos ------------------------------------------------------

	/**
	 * Salida legal que deja al fantasma más cerca de 'destino'. La distancia se mide desde
	 * el nodo vecino y con el movimiento que lleva a él. NEUTRAL si no hay salidas.
	 */
	public static MOVE irHacia(Game game, GHOST ghost, int destino) {
		int nodo = game.getGhostCurrentNodeIndex(ghost);
		MOVE[] salidas = game.getPossibleMoves(nodo, game.getGhostLastMoveMade(ghost));
		if (salidas.length == 0 || destino < 0) return MOVE.NEUTRAL;

		MOVE mejor = salidas[0];
		int min = INALCANZABLE;
		for (MOVE m : salidas) {
			int d = game.getShortestPathDistance(game.getNeighbour(nodo, m), destino, m);
			if (d < min) {
				min = d;
				mejor = m;
			}
		}
		return mejor;
	}

	/**
	 * Huida eficiente: aleja al fantasma de 'peligro' eligiendo bien el pasillo.
	 *  1. Descarta la salida por la que viene el peligro (la que más lo acerca a él).
	 *  2. Prefiere los pasillos sin otros comestibles dentro, para no huir en fila.
	 *  3. Entre los que quedan, el de más margen en su cruce final: lo que tarda el
	 *     peligro en llegar a ese cruce menos lo que tarda el fantasma (el doble si es
	 *     comestible, porque va a media velocidad).
	 *
	 * @param peligro        nodo del que se huye (Ms. Pac-Man o una power pill)
	 * @param ultimoPeligro  último movimiento del peligro, o null si es estático (una pill)
	 */
	public static MOVE huidaEficiente(Game game, GHOST ghost, int peligro, MOVE ultimoPeligro) {
		int nodo = game.getGhostCurrentNodeIndex(ghost);
		MOVE[] salidas = game.getPossibleMoves(nodo, game.getGhostLastMoveMade(ghost));
		if (salidas.length == 0 || peligro < 0) return MOVE.NEUTRAL;
		if (salidas.length == 1) return salidas[0];

		MOVE porDondeViene = salidaHaciaPeligro(game, nodo, salidas, peligro, ultimoPeligro);
		int velocidad = game.isGhostEdible(ghost) ? GhostsConstants.FACTOR_LENTITUD_COMESTIBLE : 1;

		MOVE mejor = null;
		boolean mejorLibre = false;
		int mejorMargen = Integer.MIN_VALUE;
		for (MOVE m : salidas) {
			if (m == porDondeViene) continue;
			Tramo t = siguienteCruce(game, nodo, m);
			boolean libre = !hayOtroComestible(game, ghost, t);
			int margen = distanciaDesdePeligro(game, peligro, t.cruce, ultimoPeligro) - velocidad * t.pasos;

			// Un pasillo libre siempre gana a uno con compañeros; a igualdad, más margen
			if (mejor == null || (libre && !mejorLibre) || (libre == mejorLibre && margen > mejorMargen)) {
				mejor = m;
				mejorLibre = libre;
				mejorMargen = margen;
			}
		}
		return mejor != null ? mejor : salidas[0];
	}

	// ---- Objetivos para las acciones ------------------------------------------------

	/**
	 * Próximo cruce al que llegará Ms. Pac-Man siguiendo su pasillo actual (para interceptar).
	 * Si está en un cruce o no se mueve, no se puede prever: devuelve su propio nodo.
	 */
	public static int proximoCrucePacman(Game game) {
		int nodo = game.getPacmanCurrentNodeIndex();
		MOVE ultimo = game.getPacmanLastMoveMade();
		if (ultimo == MOVE.NEUTRAL || game.isJunction(nodo)) return nodo;

		MOVE[] salidas = game.getPossibleMoves(nodo, ultimo);
		if (salidas.length == 0) return nodo;
		return siguienteCruce(game, nodo, salidas[0]).cruce;
	}

	/** Power pill activa más cercana a Ms. Pac-Man (objetivo de vigilar y bloquear). -1 si no queda. */
	public static int powerPillCercanaPacman(Game game) {
		return masCercano(game, game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade(),
				game.getActivePowerPillsIndices());
	}

	/** Píldora activa más cercana a Ms. Pac-Man (objetivo de patrullar). -1 si no queda. */
	public static int pildoraCercanaPacman(Game game) {
		return masCercano(game, game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade(),
				game.getActivePillsIndices());
	}

	/**
	 * Los cuatro cruces de esquina del laberinto, en este orden: arriba-izquierda,
	 * arriba-derecha, abajo-izquierda, abajo-derecha. Se usan cruces y no nodos cualquiera
	 * para que el fantasma pueda seguir eligiendo salida al llegar.
	 */
	public static int[] esquinas(Game game) {
		int[] esquinas = new int[4];
		int[] mejor = { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE };
		for (int c : game.getJunctionIndices()) {
			int x = game.getNodeXCood(c), y = game.getNodeYCood(c);
			int[] valores = { x + y, -x + y, x - y, -x - y };
			for (int i = 0; i < 4; i++) {
				if (valores[i] < mejor[i]) {
					mejor[i] = valores[i];
					esquinas[i] = c;
				}
			}
		}
		return esquinas;
	}

	// ---- Auxiliares privados ---------------------------------------------------------

	/** Distancia que recorre el peligro hasta 'destino': con su último movimiento si se mueve. */
	private static int distanciaDesdePeligro(Game game, int peligro, int destino, MOVE ultimoPeligro) {
		return ultimoPeligro == null
				? game.getShortestPathDistance(peligro, destino)
				: game.getShortestPathDistance(peligro, destino, ultimoPeligro);
	}

	/** La salida cuyo primer nodo queda más cerca del peligro: por ahí viene. */
	private static MOVE salidaHaciaPeligro(Game game, int nodo, MOVE[] salidas, int peligro, MOVE ultimoPeligro) {
		MOVE hacia = null;
		int min = INALCANZABLE;
		for (MOVE m : salidas) {
			int d = distanciaDesdePeligro(game, peligro, game.getNeighbour(nodo, m), ultimoPeligro);
			if (d < min) {
				min = d;
				hacia = m;
			}
		}
		return hacia;
	}

	/** ¿Hay otro fantasma comestible (fuera de la cárcel) dentro del tramo? */
	private static boolean hayOtroComestible(Game game, GHOST yo, Tramo t) {
		for (GHOST g : GHOST.values()) {
			if (g == yo || game.getGhostLairTime(g) > 0 || !game.isGhostEdible(g)) continue;
			if (t.nodos.contains(game.getGhostCurrentNodeIndex(g))) return true;
		}
		return false;
	}
}