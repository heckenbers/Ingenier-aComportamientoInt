package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman;

import java.util.HashMap;

import es.ucm.fdi.ici.fuzzy.FuzzyInput;
import pacman.game.Game;

/**
 * De la percepción a los números del FCL. Todas las entradas se mandan en todos los turnos
 * y se recortan a su rango: "no hay" es el extremo lejano del rango, nunca -1.
 *
 *   amenaza  ticks hasta que el fantasma peligroso más rápido llega a Ms. Pac-Man      0..60
 *   margen   ventaja (ticks) de la mejor ruta de huida sobre todos los fantasmas      -10..40
 *   caza     fantasmas comestibles que da tiempo a encadenar desde aquí                0..4
 *   revertir ticks hasta que un comestible vuelva a ser peligroso donde está ella      0..60
 *   cebo     fantasmas que encadenaría si se comiera ahora la píldora de cebo          0..4
 *   espera   ticks seguidos que lleva esperando junto al cebo                          0..120
 *   quedan   píldoras que quedan, en % del laberinto                                   0..100
 *   pildora  pasos hasta la píldora normal más cercana (desde la v6; 0 si no quedan)   0..100
 *   hambre   ticks sin comerse ninguna píldora (desde la v8)                           0..300
 *   espacio  nodos de laberinto "suyos" saliendo por el mejor movimiento (Territorio, v17) 0..400
 *
 * Los atributos no tienen inicializador: FuzzyInput llama a parseInput() desde su constructor,
 * y aquí los valores se calculan después, en el nuestro, a partir de la percepción ya hecha.
 */
public class MsPacManInput extends FuzzyInput {

    public static final double LEJOS_TICKS = 60;
    public static final double MARGEN_MIN = -10, MARGEN_MAX = 40;
    public static final double ESPERA_MAX = 120;
    public static final double HAMBRE_MAX = 300;
    public static final double ESPACIO_MAX = 400;

    private double amenaza, margen, caza, revertir, cebo, espera, quedan, pildora, hambre, espacio;

    public MsPacManInput(Game game, Percepcion p, MemoriaPacMan memoria) {
        super(game);
        amenaza = recortar(p.getAmenazaEnPacman(), 0, LEJOS_TICKS);
        margen = recortar(p.getMejorMargen(), MARGEN_MIN, MARGEN_MAX);
        caza = fantasmas(p.getValorPlanCaza(), Math.max(200, game.getGhostCurrentEdibleScore()));
        revertir = recortar(p.getRevertirEnPacman(), 0, LEJOS_TICKS);
        cebo = (p.getPildoraCebo() >= 0 && p.isPoderRentable()) ? fantasmas(p.getValorCebo(), 200) : 0;
        espera = recortar(memoria.getTicksEsperando(game.getTotalTime()), 0, ESPERA_MAX);
        int total = Math.max(1, game.getNumberOfPills());
        quedan = 100.0 * game.getNumberOfActivePills() / total;
        pildora = recortar(p.getDistPildora(), 0, 100);
        hambre = recortar(memoria.getTicksSinComer(game.getTotalTime()), 0, HAMBRE_MAX);
        espacio = recortar(Territorio.mayor(p), 0, ESPACIO_MAX);
    }

    /** Nada que leer del juego aquí: todo sale de la percepción, en el constructor. */
    @Override
    public void parseInput() { }

    /** Puntos de una cadena (base, 2·base, 4·base…) a número de fantasmas: v = base·(2^n − 1). */
    static double fantasmas(int valor, int base) {
        if (valor <= 0) return 0;
        return Math.min(4, Math.log(1.0 + (double) valor / base) / Math.log(2));
    }

    static double recortar(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public HashMap<String, Double> getFuzzyValues() {
        HashMap<String, Double> vars = new HashMap<>();   // la clave = el nombre en VAR_INPUT
        vars.put("amenaza", amenaza);
        vars.put("margen", margen);
        vars.put("caza", caza);
        vars.put("revertir", revertir);
        vars.put("cebo", cebo);
        vars.put("espera", espera);
        vars.put("quedan", quedan);
        vars.put("pildora", pildora);
        vars.put("hambre", hambre);
        vars.put("espacio", espacio);
        return vars;
    }
}
