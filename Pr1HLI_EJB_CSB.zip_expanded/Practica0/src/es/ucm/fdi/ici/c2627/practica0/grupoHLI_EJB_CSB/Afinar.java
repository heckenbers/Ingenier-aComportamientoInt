package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Random;

import pacman.Executor;
import pacman.controllers.GhostController;
import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.util.Stats;

/**
 * Afinado por coordenadas: se parte de los valores de Constantes y, parámetro a parámetro,
 * se prueba cada candidato contra TODOS los rivales y se queda el que mejor puntúa de media.
 * Al final se confirma el resultado con más partidas frente a los valores de partida.
 *
 * Uso: java Afinar [partidasPorRival] [partidasConfirmacion]   (por defecto 50 y 300)
 *
 * Es una herramienta externa: el controlador entregado no lee nada. Los números que salgan
 * se copian a mano en Constantes.
 */
public class Afinar {

    // Orden de los parámetros: el peso de la comida se fija a 1 (sólo importan las proporciones).
    private static final String[] NOMBRES = {
        "PESO_AMENAZA", "PESO_PRESA", "PESO_PODER_ATRAE", "PESO_PODER_EVITA",
        "CAIDA_COMIDA", "CAIDA_AMENAZA", "CAIDA_PRESA", "CAIDA_PODER"
    };

    private static final double[][] CANDIDATOS = {
        {1, 3, 6, 10, 20},
        {0, 1, 2, 4, 6},
        {1, 2, 4, 8},
        {0, 1, 2, 4},
        {10, 0, 10, 0},
        {0.75, 0.85, 0.90, 0.95},
        {0.70, 0.80, 0.90, 0.95},
        {0.70, 0.85, 0.90},
        {0.80, 0.90, 0.95},
        {0.30, 0.9, 0.9, 0.9}
    };

    private static final int PASADAS = 1;

    /** Resultado de evaluar un juego de parámetros contra todos los rivales. */
    private static final class Resultado {
        final double media;     // media de las medias por rival
        final double error;     // error típico de esa media
        final double[] porRival;
        Resultado(double media, double error, double[] porRival) {
            this.media = media;
            this.error = error;
            this.porRival = porRival;
        }
    }

    public static void main(String[] args) {
        int partidas = args.length > 0 ? Integer.parseInt(args[0]) : 50;
        int confirmacion = args.length > 1 ? Integer.parseInt(args[1]) : 100;

        Executor executor = new Executor.Builder()
                .setVisual(false)
                .setTickLimit(4000)
                .setPacmanPO(false)
                .setGhostPO(false)
                .setGhostsMessage(false)
                .build();
        GhostController[] rivales = rivales();

        double[] inicial = {
            Constantes.PESO_AMENAZA, Constantes.PESO_PRESA,
            Constantes.PESO_PODER_ATRAE, Constantes.PESO_PODER_EVITA,
            Constantes.CAIDA_COMIDA, Constantes.CAIDA_AMENAZA,
            Constantes.CAIDA_PRESA, Constantes.CAIDA_PODER
        };
        double[] actual = inicial.clone();
        Resultado mejor = evaluar(executor, rivales, actual, partidas);
        imprimir("partida", actual, mejor);

        for (int pasada = 1; pasada <= PASADAS; pasada++) {
            System.out.println("\n=== Pasada " + pasada + " ===");
            for (int i = 0; i < NOMBRES.length; i++) {
                double valorActual = actual[i];
                for (double candidato : CANDIDATOS[i]) {
                    if (candidato == valorActual) continue;
                    double[] prueba = actual.clone();
                    prueba[i] = candidato;
                    Resultado r = evaluar(executor, rivales, prueba, partidas);
                    imprimir(NOMBRES[i] + "=" + candidato, prueba, r);
                    if (r.media > mejor.media) {
                        mejor = r;
                        actual = prueba;
                    }
                }
                System.out.printf(Locale.ROOT, "--> %s se queda en %s%n", NOMBRES[i], actual[i]);
            }
        }

        System.out.println("\n=== Confirmación con " + confirmacion + " partidas por rival ===");
        Resultado a = evaluar(executor, rivales, inicial, confirmacion);
        Resultado b = evaluar(executor, rivales, actual, confirmacion);
        imprimir("valores de partida", inicial, a);
        imprimir("valores afinados", actual, b);

        double diferencia = b.media - a.media;
        double errorDiferencia = Math.sqrt(a.error * a.error + b.error * b.error);
        System.out.printf(Locale.ROOT, "%nMejora: %.0f ± %.0f (si es menor que 2 errores, es ruido)%n",
                diferencia, errorDiferencia);
        System.out.println("\nPara pegar en Constantes:");
        for (int i = 0; i < NOMBRES.length; i++) {
            System.out.printf(Locale.ROOT, "  %s = %s%n", NOMBRES[i], actual[i]);
        }
    }

    /** Rival de referencia: añade aquí los fantasmas de otros grupos (jar en el classpath). */
    private static GhostController[] rivales() {
        return new GhostController[] {
            new GhostsHLI_EJB_CSB(),                  // los tuyos
            new Perseguidores(),           // persecución directa por el camino más corto
            new Aleatorios(),
            new GhostsElkin()// movimientos legales al azar
            // , new otro.paquete.Ghosts()   <- los de otros grupos
        };
    }

    private static Resultado evaluar(Executor executor, GhostController[] rivales, double[] p, int partidas) {
        double[] medias = new double[rivales.length];
        double sumaVarianzas = 0;
        double suma = 0;
        for (int r = 0; r < rivales.length; r++) {
        	MsPacManHLI_EJB_CSB pacman = new MsPacManHLI_EJB_CSB(1.0, p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]);
            Stats[] s = executor.runExperiment(pacman, rivales[r], partidas, "afinar");
            medias[r] = s[0].getAverage();
            suma += medias[r];
            sumaVarianzas += s[0].getStandardError() * s[0].getStandardError();
        }
        double media = suma / rivales.length;
        double error = Math.sqrt(sumaVarianzas) / rivales.length;
        return new Resultado(media, error, medias);
    }

    private static void imprimir(String etiqueta, double[] p, Resultado r) {
        StringBuilder sb = new StringBuilder();
        for (double m : r.porRival) {
            sb.append(String.format(Locale.ROOT, " %.0f", m));
        }
        System.out.printf(Locale.ROOT, "%-26s media %.0f ± %.0f   por rival:%s%n",
                etiqueta, r.media, r.error, sb);
    }

    // ------------------------------------------------------------- Rivales de referencia

    /** Todos persiguen por el camino más corto (huyen si son comestibles). */
    public static class Perseguidores extends GhostController {
        @Override
        public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
            EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
            int pac = game.getPacmanCurrentNodeIndex();
            for (GHOST f : GHOST.values()) {
                if (!game.doesGhostRequireAction(f)) continue;
                int nodo = game.getGhostCurrentNodeIndex(f);
                MOVE ult = game.getGhostLastMoveMade(f);
                MOVE m = game.getGhostEdibleTime(f) > 0
                        ? game.getNextMoveAwayFromTarget(nodo, pac, ult, DM.PATH)
                        : game.getNextMoveTowardsTarget(nodo, pac, ult, DM.PATH);
                moves.put(f, m == null ? MOVE.NEUTRAL : m);
            }
            return moves;
        }
    }

    /** Todos eligen una salida legal al azar. */
    public static class Aleatorios extends GhostController {
        private final Random rnd = new Random();

        @Override
        public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
            EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
            for (GHOST f : GHOST.values()) {
                if (!game.doesGhostRequireAction(f)) continue;
                MOVE[] ops = game.getPossibleMoves(game.getGhostCurrentNodeIndex(f), game.getGhostLastMoveMade(f));
                moves.put(f, ops.length == 0 ? MOVE.NEUTRAL : ops[rnd.nextInt(ops.length)]);
            }
            return moves;
        }
    }
}