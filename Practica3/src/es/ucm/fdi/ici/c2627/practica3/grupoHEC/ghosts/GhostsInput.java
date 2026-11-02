package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts;

import static es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.GhostsConstants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesInput;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Convierte el juego en hechos de CLIPS, una vez por turno para los cuatro fantasmas.
 *
 * Reparto del trabajo: aquí se calcula todo lo que necesita caminos o bucles (distancias,
 * márgenes, refugio), con Movimientos. Las reglas del .clp solo comparan estos valores con
 * sus umbrales, y entre fantasmas (por ejemplo, "no hay otro con más margen").
 *
 * Hechos:
 *   (FANTASMA ...) uno por fantasma, iguales para los cuatro motores
 *   (MSPACMAN ...) uno
 * Ghosts añade en cada motor (YO (nombre X)) y (ULTIMA (accion Y)).
 *
 * El atributo se declara SIN inicializador: super(game) llama a parseInput() antes de que
 * se ejecuten los inicializadores de esta clase, que borrarían lo calculado.
 */
public class GhostsInput extends RulesInput {

	private List<String> facts;

	public GhostsInput(Game game) {
		super(game);                       // -> parseInput()
	}

	@Override
	public void parseInput() {
		facts = new ArrayList<>();

		// ---- Ms. Pac-Man ----------------------------------------------------------------
		int pp = Movimientos.powerPillCercanaPacman(game);
		int distPacmanPP = pp < 0 ? LEJOS : Movimientos.distanciaPacman(game, pp);

		// Atrapable: está en un pasillo (no puede darse la vuelta) y en él no hay ninguna PP
		Movimientos.Tramo pasillo = Movimientos.tramoPacman(game);
		boolean atrapable = pasillo != null && !hayPPEn(pasillo);

		facts.add(String.format("(MSPACMAN (distPPill %d) (enPasillo %s))", distPacmanPP, atrapable));

		// ---- Un hecho por fantasma -----------------------------------------------------------
		for (GHOST g : GHOST.values()) {
			boolean carcel = game.getGhostLairTime(g) > 0;
			boolean comestible = !carcel && game.isGhostEdible(g);
			int tiempo = comestible ? game.getGhostEdibleTime(g) : 0;

			int distPacman = carcel ? LEJOS : Movimientos.distanciaFantasma(game, g, game.getPacmanCurrentNodeIndex());
			int distPeligro = carcel ? LEJOS : distanciaPeligro(g, distPacman);
			int distPP = (carcel || pp < 0) ? LEJOS : Movimientos.distanciaFantasma(game, g, pp);
			int margenBloqueo = (carcel || pp < 0) ? -LEJOS : distPacmanPP - distBloqueo(g, pp);
			int margenCorte = carcel ? -LEJOS : acotar(Movimientos.intercepcion(game, g).margen);
			int margenCierre = (carcel || comestible || !atrapable) ? -LEJOS
					: pasillo.pasos - Movimientos.distanciaFantasma(game, g, pasillo.cruce);
			GHOST refugio = comestible ? companero(g, distPeligro) : null;

			facts.add(String.format(
					"(FANTASMA (nombre %s) (comestible %s) (enCarcel %s) (tiempoComestible %d)"
							+ " (distPacman %d) (distPeligro %d) (distPP %d)"
							+ " (margenBloqueo %d) (margenCorte %d) (margenCierre %d) (companero %s))",
					g, comestible, carcel, tiempo,
					distPacman, distPeligro, distPP,
					margenBloqueo, margenCorte, margenCierre, refugio == null ? "NINGUNO" : refugio));
		}
	}

	@Override
	public Collection<String> getFacts() {
		return facts;
	}

	// ==================================================================================
	// Cálculos (los mismos que en GhostsInput de la práctica 2)
	// ==================================================================================

	/**
	 * Distancia para HUIR: la menor entre la de Ms. Pac-Man al fantasma (sin darse ella la
	 * vuelta) y la del fantasma a ella. Solo la del fantasma a ella podía salir enorme si él
	 * iba alejándose por un pasillo, mientras ella lo alcanzaba por detrás.
	 */
	private int distanciaPeligro(GHOST g, int distPacman) {
		int ellaAMi = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(),
				game.getGhostCurrentNodeIndex(g), game.getPacmanLastMoveMade());
		return Math.min(ellaAMi, distPacman);
	}

	/**
	 * Distancia del fantasma a la PP para bloquear. Si ya está junto a ella (RADIO_GUARDIA_PP),
	 * la real, sin dirección: al llegar la pasa de largo y "sin darse la vuelta" parecería lejísimos.
	 */
	private int distBloqueo(GHOST g, int pp) {
		int cerca = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(g), pp);
		return cerca <= RADIO_GUARDIA_PP ? cerca : Movimientos.distanciaFantasma(game, g, pp);
	}

	/** ¿Hay alguna power pill activa en el pasillo? (ella se la comería antes del encuentro) */
	private boolean hayPPEn(Movimientos.Tramo pasillo) {
		for (int p : game.getActivePowerPillsIndices())
			if (pasillo.nodos.contains(p)) return true;
		return false;
	}

	/**
	 * (*IV) Para un comestible, el compañero no comestible más cercano al que llega antes de
	 * que Ms. Pac-Man lo alcance: 2·(dC − RADIO_SEGURIDAD) + MARGEN_REFUGIO < 2d/3, y sin que
	 * ella esté más cerca del compañero que él (estaría en medio del camino). null si no hay.
	 */
	private GHOST companero(GHOST g, int distPeligro) {
		int miNodo = game.getGhostCurrentNodeIndex(g);
		MOVE miUltimo = game.getGhostLastMoveMade(g);
		GHOST mejor = null;
		int mejorDist = Integer.MAX_VALUE;
		for (GHOST c : GHOST.values()) {
			if (c == g || game.getGhostLairTime(c) > 0 || game.isGhostEdible(c)) continue;
			int nodoC = game.getGhostCurrentNodeIndex(c);
			int dC = game.getShortestPathDistance(miNodo, nodoC, miUltimo);
			long tiempoYo = (long) FACTOR_LENTITUD_COMESTIBLE * Math.max(0, dC - RADIO_SEGURIDAD) + MARGEN_REFUGIO;
			boolean ellaEnMedio = Movimientos.distanciaPacman(game, nodoC) <= dC;
			if (!ellaEnMedio && 3L * tiempoYo < 2L * distPeligro && dC < mejorDist) {
				mejor = c;
				mejorDist = dC;
			}
		}
		return mejor;
	}

	/** Los márgenes "imposibles" de Movimientos son enormes: se dejan en ±LEJOS para que el hecho sea legible. */
	private static int acotar(int margen) {
		return Math.max(-LEJOS, Math.min(LEJOS, margen));
	}
}