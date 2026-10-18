package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts;

import static es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto.Papel;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Lo que leen las transiciones en un turno. Se construye uno por turno y lo comparten las
 * cuatro máquinas. Las transiciones solo preguntan: todo se calcula aquí, una vez.
 *
 * Orden de construcción (importante):
 *  1. super(game) llama a parseInput(): distancias y condiciones de cada fantasma.
 *  2. El cuerpo del constructor calcula el reparto del equipo, que necesita el reparto
 *     del turno anterior (histéresis). Por eso no se puede hacer dentro de parseInput().
 *
 * Los atributos se declaran SIN valor inicial: un inicializador se ejecutaría después de
 * parseInput() y borraría lo calculado.
 */
public class GhostsInput extends Input {

	private static final GHOST[] FANTASMAS = GHOST.values();

	// ---- Ms. Pac-Man y la power pill ----------------------------------------------
	private int nodoPacman;
	private MOVE ultimoPacman;
	private int pill;                 // power pill activa más cercana a Ms. Pac-Man, o -1
	private int distPacmanPill;       // distancia de Ms. Pac-Man a esa pill

	// ---- Por fantasma ---------------------------------------------------------------
	private EnumMap<GHOST, Boolean> enCarcel;
	private EnumMap<GHOST, Boolean> comestible;
	private EnumMap<GHOST, Integer> tiempoComestible;
	private EnumMap<GHOST, Integer> distAPacman;   // del fantasma a Ms. Pac-Man
	private EnumMap<GHOST, Integer> distAPill;     // del fantasma a la pill

	// ---- Reparto del equipo -----------------------------------------------------------
	private Reparto reparto;

	/**
	 * @param anterior reparto del turno anterior (el controlador guarda el último); en el
	 *                 primer turno, un Reparto vacío.
	 */
	public GhostsInput(Game game, Reparto anterior) {
		super(game);                       // -> parseInput()
		this.reparto = repartir(anterior == null ? new Reparto() : anterior);
	}

	// ==================================================================================
	// 1. Datos del turno
	// ==================================================================================

	@Override
	public void parseInput() {
		nodoPacman = game.getPacmanCurrentNodeIndex();
		ultimoPacman = game.getPacmanLastMoveMade();
		pill = Movimientos.powerPillCercanaPacman(game);
		distPacmanPill = pill < 0 ? Movimientos.INALCANZABLE
				: game.getShortestPathDistance(nodoPacman, pill, ultimoPacman);

		enCarcel = new EnumMap<>(GHOST.class);
		comestible = new EnumMap<>(GHOST.class);
		tiempoComestible = new EnumMap<>(GHOST.class);
		distAPacman = new EnumMap<>(GHOST.class);
		distAPill = new EnumMap<>(GHOST.class);

		for (GHOST g : FANTASMAS) {
			boolean carcel = game.getGhostLairTime(g) > 0;
			enCarcel.put(g, carcel);
			comestible.put(g, !carcel && game.isGhostEdible(g));
			tiempoComestible.put(g, game.getGhostEdibleTime(g));
			distAPacman.put(g, Movimientos.distanciaFantasma(game, g, nodoPacman));
			distAPill.put(g, pill < 0 ? Movimientos.INALCANZABLE : Movimientos.distanciaFantasma(game, g, pill));
		}
	}

	// ==================================================================================
	// 2. Condiciones para las transiciones
	// ==================================================================================

	// ---- Globales ----------------------------------------------------------------------

	/** "Fantasma es comido" / "Termina tiempo de cárcel" (negado). */
	public boolean enCarcel(GHOST g) { return enCarcel.get(g); }

	public boolean esComestible(GHOST g) { return comestible.get(g); }

	/**
	 * (*II) Comestible, pero Ms. Pac-Man ya no llega a comérselo: tiempoComestible + MARGEN < 2d/3.
	 * El 2d/3 sale de que se acercan 1 + 1/2 nodos por tick. Se multiplica por 3 para no redondear.
	 */
	public boolean fueraDePeligro(GHOST g) {
		if (!comestible.get(g)) return false;
		return 3L * (tiempoComestible.get(g) + MARGEN_HUIR) < 2L * distAPacman.get(g);
	}

	/** "Ms. Pacman se come PP": comestible y todavía en peligro. Excluyente con fueraDePeligro. */
	public boolean enPeligroComestible(GHOST g) {
		return comestible.get(g) && !fueraDePeligro(g);
	}

	/** "Deja de ser comestible o está fuera de peligro" (*II): vuelta a NORMAL. */
	public boolean puedeVolverANormal(GHOST g) {
		return !comestible.get(g) || fueraDePeligro(g);
	}

	// ---- PELIGRO POR PÍLDORA -------------------------------------------------------------

	/** Hay una power pill amenazada: activa y con Ms. Pac-Man a RADIO_PILL o menos. */
	public boolean hayPillAmenazada() { return pill >= 0 && distPacmanPill <= RADIO_PILL; }

	/** (*I) "Ms. Pacman está cerca de PP": hay pill amenazada y el fantasma está cerca de ella. */
	public boolean cercaDePillAmenazada(GHOST g) {
		return !enCarcel.get(g) && !comestible.get(g) && hayPillAmenazada()
				&& distAPill.get(g) <= RADIO_PELIGRO;
	}

	/** "Ms. Pacman se aleja de la PP" (con histéresis): salida de PELIGRO hacia NORMAL. */
	public boolean pacmanLejosDePill() {
		return pill < 0 || distPacmanPill > RADIO_PILL + HISTERESIS_PP;
	}

	/** (*III) Llega a la pill antes que Ms. Pac-Man. Si deja de cumplirse: BLOQUEAR -> HUIDA. */
	public boolean llegaAntesAPill(GHOST g) {
		return pill >= 0 && !enCarcel.get(g)
				&& (long) distBloqueo(g) + MARGEN_PP < distPacmanPill;
	}

	/**
	 * Distancia del fantasma a la pill para bloquear. Normalmente la de siempre (sin darse la
	 * vuelta), pero si ya está junto a la pill (RADIO_GUARDIA_PP) cuenta la distancia real: al
	 * llegar no puede pararse y la pasa de largo, y con la distancia "sin dar la vuelta" parecería
	 * que está lejísimos y abandonaría el bloqueo justo cuando lo ha conseguido.
	 */
	private int distBloqueo(GHOST g) {
		if (pill < 0 || enCarcel.get(g)) return Movimientos.INALCANZABLE;
		int cerca = game.getShortestPathDistance(game.getGhostCurrentNodeIndex(g), pill);
		return cerca <= RADIO_GUARDIA_PP ? cerca : distAPill.get(g);
	}

	/**
	 * HUIDA -> BLOQUEAR: llega antes con ventaja (histéresis) y es el no comestible más
	 * cercano a la pill. Así, como mucho un fantasma empieza a bloquear cada turno.
	 */
	public boolean esBloqueador(GHOST g) {
		if (!llegaAntesAPill(g) || (long) distBloqueo(g) + MARGEN_PP + HISTERESIS_BLOQUEO >= distPacmanPill)
			return false;
		for (GHOST otro : FANTASMAS) {
			if (otro == g || enCarcel.get(otro) || comestible.get(otro)) continue;
			int d = distBloqueo(otro), mia = distBloqueo(g);
			if (d < mia || (d == mia && otro.ordinal() < g.ordinal())) return false;
		}
		return true;
	}

	// ---- COMESTIBLE -------------------------------------------------------------------------

	/** "Ms. Pacman está cerca": vuelta a HUIDA EFICIENTE. */
	public boolean pacmanCerca(GHOST g) {
		return distAPacman.get(g) <= DISTANCIA_CERCA_PACMAN;
	}

	/** "Ms. Pacman está lejos" (con histéresis respecto a pacmanCerca). */
	public boolean pacmanLejos(GHOST g) {
		return (long) distAPacman.get(g) > DISTANCIA_CERCA_PACMAN + HISTERESIS_CERCA;
	}

	/** (*IV) "Puede refugiarse": hay compañero al que llega antes que Ms. Pac-Man a él. */
	public boolean puedeRefugiarse(GHOST g) {
		return reparto.refugio(g) != null;
	}

	// ---- NORMAL ---------------------------------------------------------------------------------

	public boolean quedanPowerPills() { return pill >= 0; }

	/** Papel del fantasma en NORMAL (nota *V). Las transiciones de NORMAL preguntan esto. */
	public Papel papel(GHOST g) { return reparto.papel(g); }

	/** El reparto de este turno: el controlador lo guarda para el siguiente y para las acciones. */
	public Reparto getReparto() { return reparto; }

	// ==================================================================================
	// 3. Reparto del equipo
	// ==================================================================================

	private Reparto repartir(Reparto anterior) {
		Reparto r = new Reparto();
		marcarPeligro(r, anterior);        // antes que los papeles: decide quién está en NORMAL
		repartirPapeles(r, anterior);
		repartirRefugios(r);
		repartirEsquinas(r, anterior);
		return r;
	}

	/**
	 * Nota *V: entre los fantasmas en NORMAL, por prioridad:
	 *  1. PERSEGUIR: el más cercano a Ms. Pac-Man.
	 *  2. INTERCEPTAR: el siguiente más cercano a Ms. Pac-Man.
	 *  3. VIGILAR: de los que quedan, el más cercano a la power pill (si queda alguna).
	 *  4. PATRULLAR: el resto.
	 * Si el que tenía un papel deja de estar en NORMAL, el papel queda libre y lo ocupa el
	 * mejor candidato en este mismo turno. Si sigue en NORMAL, solo lo pierde frente a otro
	 * que esté HISTERESIS_PAPELES nodos más cerca.
	 */
	private void repartirPapeles(Reparto r, Reparto anterior) {
		List<GHOST> libres = new ArrayList<>();
		for (GHOST g : FANTASMAS)
			if (enNormal(g, r)) libres.add(g);

		ocupar(r, anterior, libres, Papel.PERSEGUIR, distAPacman);
		ocupar(r, anterior, libres, Papel.INTERCEPTAR, distAPacman);
		if (quedanPowerPills()) ocupar(r, anterior, libres, Papel.VIGILAR, distAPill);
		for (GHOST g : libres) r.asignarPapel(g, Papel.PATRULLAR);
	}

	/** Da 'papel' al mejor de 'libres' según 'distancia', respetando al titular si no hay mucha diferencia. */
	private void ocupar(Reparto r, Reparto anterior, List<GHOST> libres, Papel papel,
			EnumMap<GHOST, Integer> distancia) {
		if (libres.isEmpty()) return;

		GHOST mejor = null;
		for (GHOST g : libres)              // libres va en orden del enum: empate -> el primero
			if (mejor == null || distancia.get(g) < distancia.get(mejor)) mejor = g;

		GHOST titular = anterior.quienTiene(papel);
		if (titular != null && libres.contains(titular)
				&& (long) distancia.get(titular) <= (long) distancia.get(mejor) + HISTERESIS_PAPELES) {
			mejor = titular;                // conserva el papel
		}
		r.asignarPapel(mejor, papel);
		libres.remove(mejor);
	}

	/** En NORMAL según la máquina: fuera de la cárcel, no comestible (o fuera de peligro) y no en PELIGRO. */
	private boolean enNormal(GHOST g, Reparto r) {
		return !enCarcel.get(g) && puedeVolverANormal(g) && !r.enPeligro(g);
	}

	/**
	 * Quién está en PELIGRO POR PÍLDORA, con las mismas reglas que la máquina:
	 *  - entra con (*I) cercaDePillAmenazada;
	 *  - sigue dentro mientras Ms. Pac-Man no se aleje de la pill (histéresis), no sea
	 *    comestible (se la ha comido) y no esté en la cárcel.
	 * Así un fantasma no recibe un papel de NORMAL mientras su máquina sigue en PELIGRO.
	 */
	private void marcarPeligro(Reparto r, Reparto anterior) {
		for (GHOST g : FANTASMAS) {
			boolean sigue = anterior.enPeligro(g) && !pacmanLejosDePill()
					&& !comestible.get(g) && !enCarcel.get(g);
			if (cercaDePillAmenazada(g) || sigue) r.marcarPeligro(g);
		}
	}

	/**
	 * (*IV) Para cada comestible, el compañero no comestible más cercano al que llega antes de
	 * que Ms. Pac-Man lo alcance: 2·(dC − RADIO_SEGURIDAD) + MARGEN < 2d/3.
	 */
	private void repartirRefugios(Reparto r) {
		for (GHOST g : FANTASMAS) {
			if (!comestible.get(g)) continue;
			int miNodo = game.getGhostCurrentNodeIndex(g);
			MOVE miUltimo = game.getGhostLastMoveMade(g);
			GHOST mejor = null;
			int mejorDist = Integer.MAX_VALUE;
			for (GHOST c : FANTASMAS) {
				if (c == g || enCarcel.get(c) || comestible.get(c)) continue;
				int dC = game.getShortestPathDistance(miNodo, game.getGhostCurrentNodeIndex(c), miUltimo);
				long tiempoYo = (long) FACTOR_LENTITUD_COMESTIBLE * Math.max(0, dC - RADIO_SEGURIDAD) + MARGEN_REFUGIO;
				if (3L * tiempoYo < 2L * distAPacman.get(g) && dC < mejorDist) {
					mejor = c;
					mejorDist = dC;
				}
			}
			if (mejor != null) r.asignarRefugio(g, mejor);
		}
	}

	/**
	 * Una esquina distinta para cada comestible:
	 *  1. Quien ya tenía esquina la conserva (si sigue libre), para no cambiar de rumbo cada turno.
	 *  2. El resto se reparte de una en una: de todas las parejas (fantasma, esquina libre),
	 *     la que da más margen = lo que tarda Ms. Pac-Man en llegar a la esquina − lo que tarda
	 *     el fantasma (a media velocidad). Así nadie huye hacia una esquina que Ms. Pac-Man tiene más cerca.
	 */
	private void repartirEsquinas(Reparto r, Reparto anterior) {
		int[] esquinas = Movimientos.esquinas(game);
		List<GHOST> sinEsquina = new ArrayList<>();

		for (GHOST g : FANTASMAS) {
			if (!comestible.get(g)) continue;
			int previa = anterior.esquina(g);
			if (previa >= 0 && !r.esquinaOcupada(previa)) r.asignarEsquina(g, previa);
			else sinEsquina.add(g);
		}

		while (!sinEsquina.isEmpty()) {
			GHOST mejorG = null;
			int mejorE = -1;
			long mejorMargen = Long.MIN_VALUE;
			for (GHOST g : sinEsquina) {
				for (int e : esquinas) {
					if (r.esquinaOcupada(e)) continue;
					long margen = (long) Movimientos.distanciaPacman(game, e)
							- (long) FACTOR_LENTITUD_COMESTIBLE * Movimientos.distanciaFantasma(game, g, e);
					if (margen > mejorMargen) {
						mejorMargen = margen;
						mejorG = g;
						mejorE = e;
					}
				}
			}
			if (mejorG == null) break;      // más comestibles que esquinas: no debería pasar
			r.asignarEsquina(mejorG, mejorE);
			sinEsquina.remove(mejorG);
		}
	}
}