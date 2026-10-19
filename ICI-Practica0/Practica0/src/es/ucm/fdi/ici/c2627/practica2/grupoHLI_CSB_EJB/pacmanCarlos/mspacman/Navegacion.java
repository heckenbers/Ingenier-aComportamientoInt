package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman;

import static pacman.game.Constants.EAT_DISTANCE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToDoubleFunction;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Utilidades de movimiento compartidas por las acciones.
 *
 * Pac-Man no puede invertir el sentido, así que sus únicas decisiones reales
 * son en los cruces. Cada movimiento posible se evalúa como una Ruta: el
 * pasillo completo hasta el siguiente cruce más PROFUNDIDAD-1 pasillos más.
 */
public final class Navegacion {

    private Navegacion() {}

    // ---------------------------------------------------------------------
    // Exploración
    // ---------------------------------------------------------------------

    public static List<Ruta> explorarDesde(MsPacManInput in, int origen, MOVE ultimo) {
        Game game = in.getGame();
        MOVE[] movs = (ultimo == MOVE.NEUTRAL) ? game.getPossibleMoves(origen) : game.getPossibleMoves(origen, ultimo);
        if (movs.length == 0)
            movs = game.getPossibleMoves(origen);
        List<Ruta> rutas = new ArrayList<>(movs.length);
        for (MOVE m : movs)
            rutas.add(explorar(in, origen, m, 0, Parametros.PROFUNDIDAD));
        return rutas;
    }

    private static Ruta explorar(MsPacManInput in, int origen, MOVE m, int offset, int prof) {
        Ruta r = recorrer(in, origen, m, offset);

        if (r.posPoder >= 0) {                  // la píldora de poder convierte a los fantasmas en presa
            r.margen = (r.margenCorredor > EAT_DISTANCE) ? Parametros.MARGEN_INFINITO : r.margenCorredor;
            return r;
        }
        if (prof <= 1 || r.margenCorredor <= Parametros.MARGEN_SEGURO) {
            r.margen = r.margenCorredor;
            return r;
        }
        MOVE[] sig = in.getGame().getPossibleMoves(r.fin, r.movFin);
        if (sig.length == 0) {                  // callejón: sin poder invertir, no hay salida
            r.margen = Math.min(r.margenCorredor, 0);
            return r;
        }
        int mejor = Integer.MIN_VALUE;
        for (MOVE s : sig)
            mejor = Math.max(mejor, explorar(in, r.fin, s, offset + r.longitud, prof - 1).margen);
        r.margen = Math.min(r.margenCorredor, mejor);
        return r;
    }

    /** Recorre un pasillo desde origen tomando m, hasta el siguiente cruce. */
    private static Ruta recorrer(MsPacManInput in, int origen, MOVE m, int offset) {
        Game game = in.getGame();
        MapaNivel mapa = in.getMapa();
        Ruta r = new Ruta();
        r.movimiento = m;
        r.movFin = m;

        int[] buf = new int[Parametros.MAX_CORREDOR];
        int k = 0;
        int margen = Integer.MAX_VALUE;
        int ultimo = origen;
        MOVE mov = m;
        int actual = game.getNeighbour(origen, m);

        while (actual >= 0 && k < Parametros.MAX_CORREDOR) {
            buf[k++] = actual;
            ultimo = actual;
            r.movFin = mov;

            if (r.posPoder < 0)
                margen = Math.min(margen, in.amenaza(actual) - (offset + k));

            int ip = game.getPillIndex(actual);
            if (ip >= 0 && Boolean.TRUE.equals(game.isPillStillAvailable(ip)))
                r.pildoras++;
            int ipp = game.getPowerPillIndex(actual);
            if (ipp >= 0 && r.posPoder < 0 && Boolean.TRUE.equals(game.isPowerPillStillAvailable(ipp))) {
                r.posPoder = k;
                r.nodoPoder = actual;
            }
            r.tunel |= mapa.esTunel(actual);
            r.colgante |= mapa.esColgante(actual);

            MOVE[] sig = game.getPossibleMoves(actual, mov);
            if (sig.length != 1)
                break;                          // cruce (>= 2 opciones) o callejón (0)
            mov = sig[0];
            actual = game.getNeighbour(actual, mov);
        }

        r.nodos = Arrays.copyOf(buf, k);
        r.longitud = k;
        r.fin = ultimo;
        r.margenCorredor = (k == 0) ? Integer.MIN_VALUE / 2 : margen;
        return r;
    }

    // ---------------------------------------------------------------------
    // Elección
    // ---------------------------------------------------------------------

    /** Mejor ruta segura según la puntuación; si ninguna es segura, la de más margen. */
    public static MOVE elegir(MsPacManInput in, ToDoubleFunction<Ruta> puntuacion) {
        Ruta mejor = null;
        double mejorP = Double.NEGATIVE_INFINITY;
        for (Ruta r : in.getRutas())
            if (r.esSegura()) {
                double p = puntuacion.applyAsDouble(r);
                if (p > mejorP) {
                    mejorP = p;
                    mejor = r;
                }
            }
        return (mejor != null) ? mejor.getMovimiento() : masSegura(in);
    }

    /** Mejor ruta según la puntuación, sin filtrar por seguridad (emergencias). */
    public static MOVE elegirSinFiltro(MsPacManInput in, ToDoubleFunction<Ruta> puntuacion) {
        Ruta mejor = null;
        double mejorP = Double.NEGATIVE_INFINITY;
        for (Ruta r : in.getRutas()) {
            double p = puntuacion.applyAsDouble(r);
            if (p > mejorP) {
                mejorP = p;
                mejor = r;
            }
        }
        return (mejor != null) ? mejor.getMovimiento() : MOVE.NEUTRAL;
    }

    /**
     * La ruta con más margen; a igualdad, la de más margen en el pasillo
     * inmediato (el único al que Pac-Man se compromete ya) y después la que
     * acaba en el nodo más abierto.
     */
    public static MOVE masSegura(MsPacManInput in) {
        MapaNivel mapa = in.getMapa();
        return elegirSinFiltro(in, r -> seguridad(r) * 100.0 + mapa.getApertura(r.getFin()));
    }

    /**
     * Seguridad de una ruta para comparar: el margen con lookahead manda, y el
     * margen del pasillo inmediato desempata. Sin esto, cuando el peor caso deja
     * todas las rutas con el mismo margen, se elegiría por orden.
     */
    public static double seguridad(Ruta r) {
        int m = Math.min(r.getMargen(), 60);
        int mc = Math.min(r.getMargenCorredor(), 60);
        return m + 0.25 * mc;
    }

    // ---------------------------------------------------------------------
    // Puntuaciones reutilizables
    // ---------------------------------------------------------------------

    /**
     * Valor de los objetivos alcanzables siguiendo la ruta:
     * max_q (1 + 0.2*min(tam(q),10)) / (1 + distancia), reducido si al llegar
     * a q algún fantasma estaría ya allí.
     */
    public static double valorObjetivos(MsPacManInput in, Ruta r, int[] objetivos, int[] tamanos) {
        Game game = in.getGame();
        double mejor = 0;
        for (int i = 0; i < objetivos.length; i++) {
            int q = objetivos[i];
            int d = r.getLongitud() + game.getShortestPathDistance(r.getFin(), q);
            int tam = (tamanos != null) ? tamanos[i] : objetivos.length;
            double v = (1.0 + 0.2 * Math.min(tam, 10)) / (1.0 + d);
            if (in.amenaza(q) - d <= Parametros.MARGEN_SEGURO)
                v *= 0.2;
            mejor = Math.max(mejor, v);
        }
        return mejor;
    }

    /** Movimiento para recoger píldoras de forma segura y sin gastar píldoras de poder. */
    public static MOVE recolectar(MsPacManInput in, int[] objetivos, int[] tamanos, double pesoMargen) {
        return elegir(in, r -> 100.0 * valorObjetivos(in, r, objetivos, tamanos)
                + 3.0 * r.getPildoras()
                + pesoMargen * Math.min(r.getMargen(), 30)
                - (r.getPosPoder() >= 0 ? Parametros.PENAL_PODER : 0)
                - (r.isColgante() ? Parametros.PENAL_COLGANTE : 0));
    }

    /** Movimiento para llegar a un nodo concreto por una ruta segura. */
    public static MOVE irA(MsPacManInput in, int destino, boolean permitirPoder) {
        Game game = in.getGame();
        return elegir(in, r -> {
            int k = r.posicionDe(destino);
            int d = (k > 0) ? k : r.getLongitud() + game.getShortestPathDistance(r.getFin(), destino);
            boolean gastaPoder = r.getPosPoder() >= 0 && r.getNodoPoder() != destino;
            return -d + 0.2 * Math.min(r.getMargen(), 30)
                    - ((gastaPoder && !permitirPoder) ? Parametros.PENAL_PODER : 0);
        });
    }
}
