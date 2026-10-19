import java.util.Arrays;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import es.ucm.fdi.ici.c2627.practica2.grupoHEC.Ghosts;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsConstants;
import es.ucm.fdi.ici.rivales.MsPacManAvanzado;
import es.ucm.fdi.ici.rivales.MsPacManBasico;
import es.ucm.fdi.ici.rivales.MsPacManIntermedio;
import pacman.Executor;
import pacman.controllers.PacmanController;
import pacman.game.util.Stats;

/**
 * Busca los mejores valores de GhostsConstants jugando partidas (no se entrega: va junto al
 * ExecutorTest, fuera del paquete del grupo).
 *
 * Método: descenso por coordenadas.
 *  1. Parte de los valores actuales de GhostsConstants.
 *  2. Para cada constante, prueba todos sus valores candidatos (las demás fijas) y se queda
 *     con el que da MENOS puntos a Ms. Pac-Man. Solo cambia el valor actual si el nuevo lo
 *     mejora en más de UMBRAL puntos: diferencias menores pueden ser ruido.
 *  3. Pasa a la siguiente constante con ese valor ya puesto. Da VUELTAS vueltas, porque
 *     las constantes se influyen entre sí.
 *  4. Compara la configuración inicial y la final con más partidas (VALIDACION).
 *
 * Cada evaluación juega PARTIDAS_POR_RIVAL contra cada Ms. Pac-Man rival y hace la media,
 * para no ajustar los fantasmas a un solo rival.
 *
 * Antes de ejecutarlo: GhostsConstants.DEBUG = false.
 * Al terminar imprime las líneas para copiar en GhostsConstants.
 */
public class Afinador {

	// ---- Configuración del experimento ------------------------------------------------

	private static final int PARTIDAS_POR_RIVAL = 40;   // por evaluación: x3 rivales
	private static final int VUELTAS = 2;
	private static final double UMBRAL = 150;           // mejora mínima (puntos) para cambiar un valor
	private static final int VALIDACION = 150;          // partidas por rival en la comprobación final

	/** Rivales contra los que se mide. Se crea uno nuevo en cada evaluación. */
	private static final List<Supplier<PacmanController>> RIVALES = Arrays.asList(
			MsPacManBasico::new,
			MsPacManIntermedio::new,
			MsPacManAvanzado::new);

	// ---- Constantes a afinar y valores candidatos ---------------------------------------

	private static final List<Parametro> PARAMETROS = Arrays.asList(
			// PELIGRO POR PÍLDORA
			new Parametro("RADIO_PILL", () -> GhostsConstants.RADIO_PILL, v -> GhostsConstants.RADIO_PILL = v,
					20, 25, 30, 35, 40),
			new Parametro("RADIO_PELIGRO", () -> GhostsConstants.RADIO_PELIGRO, v -> GhostsConstants.RADIO_PELIGRO = v,
					30, 40, 45, 50, 60),
			new Parametro("MARGEN_PP", () -> GhostsConstants.MARGEN_PP, v -> GhostsConstants.MARGEN_PP = v,
					0, 1, 2, 3, 4),
			new Parametro("HISTERESIS_BLOQUEO", () -> GhostsConstants.HISTERESIS_BLOQUEO, v -> GhostsConstants.HISTERESIS_BLOQUEO = v,
					0, 1, 2, 3, 4),
			new Parametro("RADIO_GUARDIA_PP", () -> GhostsConstants.RADIO_GUARDIA_PP, v -> GhostsConstants.RADIO_GUARDIA_PP = v,
					2, 3, 4, 6, 8),
			// COMESTIBLE
			new Parametro("MARGEN_HUIR", () -> GhostsConstants.MARGEN_HUIR, v -> GhostsConstants.MARGEN_HUIR = v,
					0, 2, 4, 8, 12),
			new Parametro("DISTANCIA_CERCA_PACMAN", () -> GhostsConstants.DISTANCIA_CERCA_PACMAN, v -> GhostsConstants.DISTANCIA_CERCA_PACMAN = v,
					10, 15, 20, 25, 30),
			new Parametro("MARGEN_REFUGIO", () -> GhostsConstants.MARGEN_REFUGIO, v -> GhostsConstants.MARGEN_REFUGIO = v,
					0, 2, 4, 8),
			// NORMAL
			new Parametro("RADIO_INTERCEPTAR_PP", () -> GhostsConstants.RADIO_INTERCEPTAR_PP, v -> GhostsConstants.RADIO_INTERCEPTAR_PP = v,
					30, 40, 50, 70, 100),
			new Parametro("MARGEN_INTERCEPCION", () -> GhostsConstants.MARGEN_INTERCEPCION, v -> GhostsConstants.MARGEN_INTERCEPCION = v,
					0, 1, 2, 3, 4),
			new Parametro("HISTERESIS_PAPELES", () -> GhostsConstants.HISTERESIS_PAPELES, v -> GhostsConstants.HISTERESIS_PAPELES = v,
					0, 2, 3, 5, 8),
			new Parametro("MARGEN_CIERRE", () -> GhostsConstants.MARGEN_CIERRE, v -> GhostsConstants.MARGEN_CIERRE = v,
					-2, -1, 0, 1, 2));

	// ==================================================================================

	public static void main(String[] args) {
		if (GhostsConstants.DEBUG) {
			System.err.println("Pon GhostsConstants.DEBUG = false antes de afinar.");
			return;
		}
		int[] iniciales = valoresActuales();
		long inicio = System.currentTimeMillis();

		for (int vuelta = 1; vuelta <= VUELTAS; vuelta++) {
			System.out.println("\n========== VUELTA " + vuelta + " de " + VUELTAS + " ==========");
			for (Parametro p : PARAMETROS)
				afinar(p, inicio);
		}

		// Comprobación final con más partidas: ¿de verdad es mejor que lo de partida?
		int[] finales = valoresActuales();
		System.out.println("\n========== VALIDACIÓN (" + VALIDACION + " partidas por rival) ==========");
		ponerValores(iniciales);
		double mediaInicial = evaluar(VALIDACION);
		ponerValores(finales);
		double mediaFinal = evaluar(VALIDACION);
		System.out.printf("Configuración inicial: %.0f puntos%n", mediaInicial);
		System.out.printf("Configuración final:   %.0f puntos%n", mediaFinal);

		System.out.println("\n========== VALORES PARA GhostsConstants ==========");
		if (mediaFinal < mediaInicial) {
			for (Parametro p : PARAMETROS)
				System.out.println("public static int " + p.nombre + " = " + p.get() + ";");
		} else {
			System.out.println("La configuración final no mejora a la inicial: deja los valores como estaban.");
		}
		System.out.printf("%nTiempo total: %.1f min%n", (System.currentTimeMillis() - inicio) / 60000.0);
	}

	/**
	 * Prueba todos los valores de 'p' (el actual también, con partidas nuevas, para comparar
	 * en igualdad) y deja puesto el mejor. Solo cambia si mejora al actual en más de UMBRAL.
	 */
	private static void afinar(Parametro p, long inicio) {
		int actual = p.get();
		double mediaActual = Double.NaN;
		int mejorValor = actual;
		double mejorMedia = Double.MAX_VALUE;

		System.out.println("\n-- " + p.nombre + " (actual: " + actual + ")");
		for (int v : p.valores) {
			p.set(v);
			double media = evaluar(PARTIDAS_POR_RIVAL);
			System.out.printf("   %s = %-4d -> %6.0f puntos   (%.1f min)%n", p.nombre, v, media,
					(System.currentTimeMillis() - inicio) / 60000.0);
			if (v == actual) mediaActual = media;
			if (media < mejorMedia) {
				mejorMedia = media;
				mejorValor = v;
			}
		}
		// Si el actual no estaba entre los candidatos, se mide aparte
		if (Double.isNaN(mediaActual)) {
			p.set(actual);
			mediaActual = evaluar(PARTIDAS_POR_RIVAL);
		}

		int elegido = (mejorMedia < mediaActual - UMBRAL) ? mejorValor : actual;
		p.set(elegido);
		System.out.println("   => " + p.nombre + " = " + elegido
				+ (elegido == actual ? " (se mantiene)" : " (cambia, antes " + actual + ")"));
	}

	/** Media de puntos de Ms. Pac-Man contra nuestros fantasmas, promediando los rivales. */
	private static double evaluar(int partidasPorRival) {
		Executor executor = new Executor.Builder()
				.setTickLimit(4000)
				.setVisual(false)
				.build();
		double suma = 0;
		for (Supplier<PacmanController> rival : RIVALES) {
			Stats[] stats = executor.runExperiment(rival.get(), new Ghosts(), partidasPorRival, "afinado");
			suma += stats[0].getAverage();
		}
		return suma / RIVALES.size();
	}

	private static int[] valoresActuales() {
		int[] valores = new int[PARAMETROS.size()];
		for (int i = 0; i < valores.length; i++) valores[i] = PARAMETROS.get(i).get();
		return valores;
	}

	private static void ponerValores(int[] valores) {
		for (int i = 0; i < valores.length; i++) PARAMETROS.get(i).set(valores[i]);
	}

	/** Una constante de GhostsConstants: cómo leerla, cómo cambiarla y qué valores probar. */
	private static final class Parametro {
		final String nombre;
		final IntSupplier getter;
		final IntConsumer setter;
		final int[] valores;

		Parametro(String nombre, IntSupplier getter, IntConsumer setter, int... valores) {
			this.nombre = nombre;
			this.getter = getter;
			this.setter = setter;
			this.valores = valores;
		}

		int get() { return getter.getAsInt(); }

		void set(int v) { setter.accept(v); }
	}
}