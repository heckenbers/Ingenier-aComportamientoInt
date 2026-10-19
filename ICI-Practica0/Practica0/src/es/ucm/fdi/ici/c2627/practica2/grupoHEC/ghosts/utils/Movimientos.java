package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import pacman.game.Constants;
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
		public final int cruce;             // cruce donde termina
		public final MOVE llegada;          // movimiento con el que se llega a ese cruce
		public final int pasos;             // nodos recorridos, cruce incluido
		public final List<Integer> nodos;   // los nodos del tramo, en orden

		Tramo(int cruce, MOVE llegada, int pasos, List<Integer> nodos) {
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
		return new Tramo(fin, dir, nodos.size(), nodos);
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
	 * Como irHacia, pero flanqueando: entre las salidas legales, prefiere las que llegan a
	 * 'destino' sin pasar por ningún nodo de 'evitar', aunque su camino sea más largo.
	 * Si todas pasan por alguno, se queda con la más corta (como irHacia).
	 * El propio 'destino' no cuenta como cruce con 'evitar'.
	 */
	public static MOVE irHaciaFlanqueando(Game game, GHOST ghost, int destino, Set<Integer> evitar) {
		int nodo = game.getGhostCurrentNodeIndex(ghost);
		MOVE[] salidas = game.getPossibleMoves(nodo, game.getGhostLastMoveMade(ghost));
		if (salidas.length == 0 || destino < 0) return MOVE.NEUTRAL;

		MOVE mejor = null, mejorCualquiera = salidas[0];
		int min = INALCANZABLE, minCualquiera = INALCANZABLE;
		for (MOVE m : salidas) {
			int vecino = game.getNeighbour(nodo, m);
			int d = game.getShortestPathDistance(vecino, destino, m);
			if (d < minCualquiera) {
				minCualquiera = d;
				mejorCualquiera = m;
			}
			if (d < min && !cruza(game, vecino, destino, m, evitar)) {
				min = d;
				mejor = m;
			}
		}
		return mejor != null ? mejor : mejorCualquiera;
	}

	/** Si el camino más corto de 'desde' a 'destino' (sin darse la vuelta) pasa por algún nodo de 'evitar'. */
	private static boolean cruza(Game game, int desde, int destino, MOVE ultimo, Set<Integer> evitar) {
		if (desde != destino && evitar.contains(desde)) return true;
		for (int n : game.getShortestPath(desde, destino, ultimo))
			if (n != destino && evitar.contains(n)) return true;
		return false;
	}

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
	 * Huida de una power pill amenazada teniendo en cuenta la inversión del motor: en el turno
	 * en que Ms. Pac-Man se come la PP, todos los fantasmas se dan la vuelta a la fuerza.
	 * Un fantasma que huye alejándose de la PP acaba, tras la inversión, yendo hacia ella.
	 *
	 * Para cada salida se mira dónde estará el fantasma cuando ella llegue a la PP
	 * ('ticksPacman' turnos, a velocidad normal porque aún no es comestible):
	 *  - Si para entonces ya ha llegado al siguiente cruce, la inversión no le afecta en este
	 *    pasillo: margen = (ticksPacman + distancia PP→cruce) − pasos hasta el cruce.
	 *  - Si la inversión le pilla dentro del pasillo, vuelve al cruce actual a media velocidad:
	 *    margen = (ticksPacman + distancia PP→cruce actual) − (ticksPacman + 2·ticksPacman).
	 *    Si en ese punto queda a EAT_DISTANCE de la PP o menos, se lo come al instante: descartada.
	 * Ambos márgenes son "cuánto antes llega el fantasma que ella al siguiente punto de decisión".
	 * A igual margen gana la salida cuyo cruce queda más lejos de la PP.
	 *
	 * Así, con la PP a punto de caer, puede salir hacia el lado del peligro (sin acercarse
	 * demasiado): la inversión le deja mirando hacia fuera y le impide volver por esa salida.
	 */
	public static MOVE huidaAnticipandoInversion(Game game, GHOST ghost, int pill, int ticksPacman) {
		int nodo = game.getGhostCurrentNodeIndex(ghost);
		MOVE[] salidas = game.getPossibleMoves(nodo, game.getGhostLastMoveMade(ghost));
		if (salidas.length == 0 || pill < 0) return MOVE.NEUTRAL;
		if (salidas.length == 1) return salidas[0];

		int lento = GhostsConstants.FACTOR_LENTITUD_COMESTIBLE;
		long pillAqui = game.getShortestPathDistance(pill, nodo);

		MOVE mejor = salidas[0];
		long mejorMargen = Long.MIN_VALUE;
		int mejorLejania = Integer.MIN_VALUE;
		for (MOVE m : salidas) {
			Tramo t = siguienteCruce(game, nodo, m);
			int lejania = game.getShortestPathDistance(pill, t.cruce);
			long margen;
			if (ticksPacman >= t.pasos) {
				margen = (long) ticksPacman + lejania - t.pasos;
			} else {
				int donde = t.nodos.get(Math.max(0, ticksPacman - 1));     // dónde le pilla la inversión
				if (game.getShortestPathDistance(pill, donde) <= Constants.EAT_DISTANCE) continue;
				margen = ticksPacman + pillAqui - (long) (1 + lento) * ticksPacman;
			}
			if (margen > mejorMargen || (margen == mejorMargen && lejania > mejorLejania)) {
				mejor = m;
				mejorMargen = margen;
				mejorLejania = lejania;
			}
		}
		return mejor;
	}

	/**
	 * Huida eficiente: aleja al fantasma de 'peligro' eligiendo bien el pasillo.
	 *  1. Descarta la salida por la que viene el peligro (la que más lo acerca a él).
	 *  2. Valora cada salida mirando DOS cruces por delante (razonar por tramos): su margen
	 *     es el peor entre el del primer cruce y el del mejor segundo cruce al que se puede
	 *     seguir desde él. Así no elige un cruce seguro desde el que todas las salidas son malas.
	 *     Margen en un cruce = lo que tarda el peligro en llegar menos lo que tarda el
	 *     fantasma (el doble si es comestible, porque va a media velocidad).
	 *  3. Orden de preferencia: primero las salidas seguras (margen > 0); entre ellas, los
	 *     pasillos sin otros comestibles (para no huir en fila); y después, más margen.
	 *     Antes "sin comestibles" iba primero y podía elegir un pasillo vacío que iba hacia ella.
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
		int restante = game.isGhostEdible(ghost) ? game.getGhostEdibleTime(ghost) : 0;

		MOVE mejor = null;
		boolean mejorSeguro = false, mejorLibre = false;
		int mejorMargen = Integer.MIN_VALUE;
		for (MOVE m : salidas) {
			if (m == porDondeViene) continue;
			Tramo t = siguienteCruce(game, nodo, m);
			int margen = margenDosCruces(game, peligro, ultimoPeligro, t, restante);
			boolean seguro = margen > 0;
			boolean libre = !hayOtroComestible(game, ghost, t);

			if (mejor == null || esMejor(seguro, libre, margen, mejorSeguro, mejorLibre, mejorMargen)) {
				mejor = m;
				mejorSeguro = seguro;
				mejorLibre = libre;
				mejorMargen = margen;
			}
		}
		return mejor != null ? mejor : salidas[0];
	}

	/** Orden de la huida: seguro, luego libre de comestibles, luego más margen. */
	private static boolean esMejor(boolean seguro, boolean libre, int margen,
			boolean mejorSeguro, boolean mejorLibre, int mejorMargen) {
		if (seguro != mejorSeguro) return seguro;
		if (libre != mejorLibre) return libre;
		return margen > mejorMargen;
	}

	/**
	 * Margen de huir por el tramo 't' mirando dos cruces: min(margen en su cruce, mejor margen
	 * en el cruce siguiente). Un tramo que pasa por el propio peligro no vale nada.
	 * 'restante' = turnos que le quedan de comestible (0 si no lo es).
	 */
	private static int margenDosCruces(Game game, int peligro, MOVE ultimoPeligro, Tramo t, int restante) {
		if (t.nodos.contains(peligro)) return Integer.MIN_VALUE / 2;
		int primero = margenEnCruce(game, peligro, ultimoPeligro, t.cruce, t.pasos, restante);

		int mejorSegundo = Integer.MIN_VALUE;
		for (MOVE m : game.getPossibleMoves(t.cruce, t.llegada)) {
			Tramo t2 = siguienteCruce(game, t.cruce, m);
			if (t2.nodos.contains(peligro)) continue;
			int segundo = margenEnCruce(game, peligro, ultimoPeligro, t2.cruce, t.pasos + t2.pasos, restante);
			mejorSegundo = Math.max(mejorSegundo, segundo);
		}
		return mejorSegundo == Integer.MIN_VALUE ? primero : Math.min(primero, mejorSegundo);
	}

	/**
	 * Margen en un cruce que está a 'nodos' del fantasma: turnos que tarda ella en llegar menos
	 * los que tarda él (tiempoFantasma: a media velocidad mientras sea comestible).
	 * Si es comestible y ella llega cuando ya ha dejado de serlo, el cruce es seguro aunque
	 * llegue antes que él: el margen es también (llegada de ella − turnos de comestible).
	 */
	private static int margenEnCruce(Game game, int peligro, MOVE ultimoPeligro, int cruce, int nodos, int restante) {
		int ella = distanciaDesdePeligro(game, peligro, cruce, ultimoPeligro);
		int margen = ella - tiempoFantasma(nodos, restante);
		return restante > 0 ? Math.max(margen, ella - restante) : margen;
	}

	/**
	 * Turnos que tarda un fantasma en recorrer 'nodos' si le quedan 'restante' turnos de
	 * comestible: mientras lo es avanza medio nodo por turno; después, uno por turno.
	 */
	public static int tiempoFantasma(int nodos, int restante) {
		if (restante <= 0) return nodos;
		int lento = GhostsConstants.FACTOR_LENTITUD_COMESTIBLE;
		int nodosLentos = restante / lento;          // los que recorre antes de dejar de ser comestible
		return nodos <= nodosLentos ? lento * nodos : restante + (nodos - nodosLentos);
	}

	// ---- Objetivos para las acciones ------------------------------------------------

	/** Dónde ir a cortar a Ms. Pac-Man y con cuánta ventaja (turnos; negativo si no llega antes). */
	public static final class Intercepcion {
		public final int objetivo;
		public final int margen;

		Intercepcion(int objetivo, int margen) {
			this.objetivo = objetivo;
			this.margen = margen;
		}
	}

	/**
	 * Punto de intercepción para 'ghost':
	 *  1. El camino que se espera de Ms. Pac-Man: hacia la power pill más cercana si está a
	 *     RADIO_INTERCEPTAR_PP o menos (va a por ella); si no, hasta su siguiente cruce.
	 *  2. Recorriendo ese camino desde ella, el primer nodo al que el fantasma llega
	 *     MARGEN_INTERCEPCION turnos antes que ella: ahí se encuentran de frente, lo antes posible.
	 *  3. Si no llega antes a ninguno, su siguiente cruce (como antes), con el mejor margen
	 *     encontrado (negativo).
	 * El margen sirve también para decidir quién de la pareja de caza intercepta.
	 */
	public static Intercepcion intercepcion(Game game, GHOST ghost) {
		int pacman = game.getPacmanCurrentNodeIndex();
		MOVE ultimo = game.getPacmanLastMoveMade();
		int cruce = proximoCrucePacman(game);
		if (game.getGhostLairTime(ghost) > 0) return new Intercepcion(cruce, Integer.MIN_VALUE / 2);

		int pp = powerPillCercanaPacman(game);
		int destino = (pp >= 0 && distanciaPacman(game, pp) <= GhostsConstants.RADIO_INTERCEPTAR_PP) ? pp : cruce;
		int[] camino = destino == pacman ? new int[0] : game.getShortestPath(pacman, destino, ultimo);

		// Un fantasma comestible (fuera de peligro) también puede interceptar, pero solo si el
		// encuentro es cuando ya ha dejado de serlo; y mientras lo es, va a media velocidad
		int restante = game.isGhostEdible(ghost) ? game.getGhostEdibleTime(ghost) : 0;

		int mejorMargen = Integer.MIN_VALUE / 2;
		for (int n : camino) {
			int ella = game.getShortestPathDistance(pacman, n, ultimo);
			int nodos = distanciaFantasma(game, ghost, n);
			if (nodos == INALCANZABLE) continue;
			if (restante > 0 && ella <= restante + GhostsConstants.MARGEN_HUIR) continue;
			int margen = ella - tiempoFantasma(nodos, restante);
			if (margen >= GhostsConstants.MARGEN_INTERCEPCION) return new Intercepcion(n, margen);
			mejorMargen = Math.max(mejorMargen, margen);
		}
		return new Intercepcion(cruce, mejorMargen);
	}

	/**
	 * Próximo cruce al que llegará Ms. Pac-Man siguiendo su pasillo actual (para interceptar).
	 * Si está en un cruce o no se mueve, no se puede prever: devuelve su propio nodo.
	 */
	public static int proximoCrucePacman(Game game) {
		Tramo t = tramoPacman(game);
		return t == null ? game.getPacmanCurrentNodeIndex() : t.cruce;
	}

	/**
	 * El pasillo que le queda a Ms. Pac-Man hasta su siguiente cruce, o null si está en un
	 * cruce o parada. En este motor ella tampoco puede darse la vuelta (correctPacManDir):
	 * dentro de un pasillo está obligada a llegar a ese cruce, así que quien lo ocupe antes la atrapa.
	 */
	public static Tramo tramoPacman(Game game) {
		int nodo = game.getPacmanCurrentNodeIndex();
		MOVE ultimo = game.getPacmanLastMoveMade();
		if (ultimo == MOVE.NEUTRAL || game.isJunction(nodo)) return null;

		MOVE[] salidas = game.getPossibleMoves(nodo, ultimo);
		if (salidas.length == 0) return null;
		return siguienteCruce(game, nodo, salidas[0]);
	}

	/** Power pill activa más cercana a Ms. Pac-Man (objetivo de vigilar y bloquear). -1 si no queda. */
	public static int powerPillCercanaPacman(Game game) {
		return masCercano(game, game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade(),
				game.getActivePowerPillsIndices());
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