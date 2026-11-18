package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts;

import static es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.GhostsConstants.*;

import java.util.HashMap;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Convierte el estado del juego desde el punto de vista de un fantasma ('yo')
 * en variables numéricas acotadas para el sistema borroso.
 */
public class GhostsInput extends FuzzyInput {

	public static final double DIST_MAX = 150.0;
	public static final double MARGEN_MIN = -50.0;
	public static final double MARGEN_MAX = 50.0;

	private final GHOST yo;
	private final String ultimaAccion;

	// Atributos SIN inicializador: super(game) llama a parseInput() antes de ejecutarlos
	private double pacman;            // distancia de 'yo' a Ms. Pac-Man [0..150]
	private double distPeligro;       // menor distancia entre ella->yo y yo->ella [0..150]
	private double ppill;             // distancia de Ms. Pac-Man a su PP más cercana [0..150]
	private double distPP;            // distancia de 'yo' a esa PP [0..150]
	private double comestible;        // turnos restantes de comestible (0 si no lo es)
	private double margenBloqueo;     // ventaja en turnos para llegar antes a la PP [-50..50]
	private double soyMejorBloqueo;   // 1.0 si ningún otro no comestible tiene más margenBloqueo, 0.0 si no
	private double margenCorte;       // ventaja en turnos para interceptar su camino [-50..50]
	private double soyMejorCorte;     // 1.0 si soy el mejor interceptor (no perseguidor), 0.0 si no
	private double margenCierre;      // ventaja para cerrar pasillo sin PP [-50..50] (-50 si no atrapable)
	private double soyMejorCierre;    // 1.0 si soy el mejor para cerrar el pasillo, 0.0 si no
	private double puestoCercania;    // ranking entre no comestibles fuera de cárcel: 1 (más cercano) a 4
	private double tieneRefugio;      // 1.0 si hay compañero seguro para refugiarse, 0.0 si no
	private double estabaBloqueando;  // 1.0 si en el turno anterior ganó 'bloquearPP', 0.0 si no

	public GhostsInput(Game game, GHOST yo) {
		this(game, yo, "");
	}

	public GhostsInput(Game game, GHOST yo, String ultimaAccion) {
		super(game);
		this.yo = yo;
		this.ultimaAccion = ultimaAccion == null ? "" : ultimaAccion;
		parseInput();
	}

	@Override
	public void parseInput() {
		if (yo == null) return; // llamado desde super(game) antes de asignar 'yo'

		boolean carcel = game.getGhostLairTime(yo) > 0;
		boolean esComestible = !carcel && game.isGhostEdible(yo);
		comestible = esComestible ? game.getGhostEdibleTime(yo) : 0.0;

		int pp = Movimientos.powerPillCercanaPacman(game);
		ppill = pp < 0 ? DIST_MAX : acotarDist(Movimientos.distanciaPacman(game, pp));

		int dPac = carcel ? (int) DIST_MAX : Movimientos.distanciaFantasma(game, yo, game.getPacmanCurrentNodeIndex());
		pacman = acotarDist(dPac);
		distPeligro = carcel ? DIST_MAX : acotarDist(distanciaPeligro(yo, dPac));
		distPP = (carcel || pp < 0) ? DIST_MAX : acotarDist(Movimientos.distanciaFantasma(game, yo, pp));

		// Cálculo de márgenes para 'yo'
		int mBloqYo = (carcel || pp < 0) ? -LEJOS : Movimientos.distanciaPacman(game, pp) - distBloqueo(yo, pp);
		int mCorteYo = carcel ? -LEJOS : Movimientos.intercepcion(game, yo).margen;

		Movimientos.Tramo pasillo = Movimientos.tramoPacman(game);
		boolean atrapable = pasillo != null && !hayPPEn(pasillo);
		int mCierreYo = (carcel || esComestible || !atrapable)
				? -LEJOS
				: pasillo.pasos - Movimientos.distanciaFantasma(game, yo, pasillo.cruce);

		margenBloqueo = acotarMargen(mBloqYo);
		margenCorte = acotarMargen(mCorteYo);
		margenCierre = acotarMargen(mCierreYo);

		// Comparación relativa con el resto de fantasmas activos no comestibles
		int masCercanosQueYo = 0;
		boolean otroMejorBloq = false;
		boolean otroMejorCierre = false;
		GHOST perseguidor = fantasmaMasCercano();

		for (GHOST otro : GHOST.values()) {
			if (otro == yo || game.getGhostLairTime(otro) > 0 || game.isGhostEdible(otro)) continue;

			int dOtro = Movimientos.distanciaFantasma(game, otro, game.getPacmanCurrentNodeIndex());
			if (dOtro < dPac) masCercanosQueYo++;

			if (pp >= 0) {
				int mBloqOtro = Movimientos.distanciaPacman(game, pp) - distBloqueo(otro, pp);
				if (mBloqOtro > mBloqYo) otroMejorBloq = true;
			}
			if (atrapable) {
				int mCierreOtro = pasillo.pasos - Movimientos.distanciaFantasma(game, otro, pasillo.cruce);
				if (mCierreOtro > mCierreYo) otroMejorCierre = true;
			}
		}

		boolean otroMejorCorte = false;
		for (GHOST otro : GHOST.values()) {
			if (otro == yo || otro == perseguidor || game.getGhostLairTime(otro) > 0 || game.isGhostEdible(otro)) continue;
			if (Movimientos.intercepcion(game, otro).margen > mCorteYo) otroMejorCorte = true;
		}

		puestoCercania = carcel ? 4.0 : (1.0 + masCercanosQueYo);
		soyMejorBloqueo = (!carcel && !esComestible && !otroMejorBloq) ? 1.0 : 0.0;
		soyMejorCorte = (!carcel && !esComestible && yo != perseguidor && !otroMejorCorte) ? 1.0 : 0.0;
		soyMejorCierre = (!carcel && !esComestible && atrapable && !otroMejorCierre) ? 1.0 : 0.0;
		tieneRefugio = (esComestible && existeCompaneroRefugio(yo, (int) distPeligro)) ? 1.0 : 0.0;
		estabaBloqueando = "bloquearPP".equals(ultimaAccion) ? 1.0 : 0.0;
	}

	@Override
	public HashMap<String, Double> getFuzzyValues() {
		HashMap<String, Double> vars = new HashMap<>();
		vars.put("pacman", pacman);
		vars.put("distPeligro", distPeligro);
		vars.put("ppill", ppill);
		vars.put("distPP", distPP);
		vars.put("comestible", comestible);
		vars.put("margenBloqueo", margenBloqueo);
		vars.put("soyMejorBloqueo", soyMejorBloqueo);
		vars.put("margenCorte", margenCorte);
		vars.put("soyMejorCorte", soyMejorCorte);
		vars.put("margenCierre", margenCierre);
		vars.put("soyMejorCierre", soyMejorCierre);
		vars.put("puestoCercania", puestoCercania);
		vars.put("tieneRefugio", tieneRefugio);
		vars.put("estabaBloqueando", estabaBloqueando);
		return vars;
	}

	// ---- Auxiliares heredados de P3 ------------------------------------------------

	private GHOST fantasmaMasCercano() {
		GHOST mejor = null;
		int min = Integer.MAX_VALUE;
		for (GHOST g : GHOST.values()) {
			if (game.getGhostLairTime(g) > 0 || game.isGhostEdible(g)) continue;
			int d = Movimientos.distanciaFantasma(game, g, game.getPacmanCurrentNodeIndex());
			if (d < min) {
				min = d;
				mejor = g;
			}
		}
		return mejor;
	}

	private int distanciaPeligro(GHOST g, int distPacman) {
		int ellaAMi = game.getShortestPathDistance(
				game.getPacmanCurrentNodeIndex(),
				game.getGhostCurrentNodeIndex(g),
				game.getPacmanLastMoveMade());
		return Math.min(ellaAMi, distPacman);
	}

	private int distBloqueo(GHOST g, int pp) {
		int cerca = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(g), pp);
		return cerca <= RADIO_GUARDIA_PP ? cerca : Movimientos.distanciaFantasma(game, g, pp);
	}

	private boolean hayPPEn(Movimientos.Tramo pasillo) {
		for (int p : game.getActivePowerPillsIndices())
			if (pasillo.nodos.contains(p)) return true;
		return false;
	}

	private boolean existeCompaneroRefugio(GHOST g, int dPeligro) {
		int miNodo = game.getGhostCurrentNodeIndex(g);
		MOVE miUltimo = game.getGhostLastMoveMade(g);
		for (GHOST c : GHOST.values()) {
			if (c == g || game.getGhostLairTime(c) > 0 || game.isGhostEdible(c)) continue;
			int nodoC = game.getGhostCurrentNodeIndex(c);
			int dC = game.getShortestPathDistance(miNodo, nodoC, miUltimo);
			long tiempoYo = (long) FACTOR_LENTITUD_COMESTIBLE * Math.max(0, dC - RADIO_SEGURIDAD) + MARGEN_REFUGIO;
			boolean ellaEnMedio = Movimientos.distanciaPacman(game, nodoC) <= dC;
			if (!ellaEnMedio && 3L * tiempoYo < 2L * dPeligro) return true;
		}
		return false;
	}

	private static double acotarDist(int d) {
		if (d < 0) return DIST_MAX;
		return Math.min(DIST_MAX, d);
	}

	private static double acotarMargen(int m) {
		return Math.max(MARGEN_MIN, Math.min(MARGEN_MAX, m));
	}
}