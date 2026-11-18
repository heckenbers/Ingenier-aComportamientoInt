package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman;

import static pacman.game.Constants.EAT_DISTANCE;

import java.util.ArrayDeque;
import java.util.Arrays;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * El "territorio" de Ms. Pac-Man: los nodos a los que llega antes que cualquier fantasma, con
 * margen, si sale por un movimiento dado. Lo usan la acción territorio (v14) y la entrada espacio
 * (v17). Sólo funciones, sin estado.
 *
 * Búsqueda en anchura desde el nodo al que lleva el movimiento, sin volver al nodo del que se
 * viene (Ms. Pac-Man no puede dar media vuelta en este motor) y pasando sólo por nodos seguros
 * según Percepcion.amenaza(n).
 */
public final class Territorio {

    public static final int RADIO = 120;            // pasos como mucho
    public static final int MARGEN = EAT_DISTANCE + 1;

    private Territorio() { }

    /** Movimientos que puede hacer ahora (sin dar media vuelta). */
    public static MOVE[] movimientos(Percepcion in) {
        Game game = in.getGame();
        int pac = game.getPacmanCurrentNodeIndex();
        MOVE ultimo = in.getUltimoMov();
        MOVE[] movs = (ultimo == MOVE.NEUTRAL) ? game.getPossibleMoves(pac) : game.getPossibleMoves(pac, ultimo);
        return movs.length == 0 ? game.getPossibleMoves(pac) : movs;
    }

    /**
     * Nodos seguros alcanzables saliendo por m, más pesoPoder por cada power pill dentro.
     * 0 si el primer nodo ya no es seguro.
     */
    public static double tamano(Percepcion in, MOVE m, double pesoPoder) {
        return tamano(in, m, pesoPoder, 0);
    }

    /** Igual, más pesoPildora por cada píldora normal dentro de la zona (desde la v18). */
    public static double tamano(Percepcion in, MOVE m, double pesoPoder, double pesoPildora) {
        Game game = in.getGame();
        int pac = game.getPacmanCurrentNodeIndex();
        int primero = game.getNeighbour(pac, m);
        if (primero < 0 || 1 + MARGEN >= in.amenaza(primero))
            return 0;
        int n = game.getNumberOfNodes();
        int[] dist = new int[n];
        Arrays.fill(dist, -1);
        int[] padre = new int[n];
        ArrayDeque<Integer> cola = new ArrayDeque<>();
        dist[pac] = 0;
        dist[primero] = 1;
        padre[primero] = pac;
        cola.add(primero);
        double valor = 0;
        while (!cola.isEmpty()) {
            int x = cola.poll();
            valor += 1;
            if (pesoPildora > 0) {
                int pi = game.getPillIndex(x);
                if (pi >= 0 && game.isPillStillAvailable(pi))
                    valor += pesoPildora;
            }
            if (pesoPoder > 0) {
                int pp = game.getPowerPillIndex(x);
                if (pp >= 0 && game.isPowerPillStillAvailable(pp))
                    valor += pesoPoder;
            }
            if (dist[x] >= RADIO) continue;
            for (int y : game.getNeighbouringNodes(x)) {
                if (y == padre[x] || dist[y] >= 0) continue;
                int t = dist[x] + 1;
                if (t + MARGEN >= in.amenaza(y)) continue;      // un fantasma llega antes: no es suyo
                dist[y] = t;
                padre[y] = x;
                cola.add(y);
            }
        }
        return valor;
    }

    /** El mayor territorio de entre los movimientos posibles (sin contar power pills). */
    public static double mayor(Percepcion in) {
        double mejor = 0;
        for (MOVE m : movimientos(in))
            mejor = Math.max(mejor, tamano(in, m, 0));
        return mejor;
    }
}
