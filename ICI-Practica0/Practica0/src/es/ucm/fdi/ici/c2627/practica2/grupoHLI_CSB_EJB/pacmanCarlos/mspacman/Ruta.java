package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman;

import pacman.game.Constants.MOVE;

/**
 * Lo que pasa si Pac-Man toma un movimiento: como no puede invertir el sentido,
 * queda comprometida a recorrer el pasillo entero hasta el siguiente cruce.
 *
 * margenCorredor = min_k (amenaza(nodo_k) - k): ticks de ventaja sobre el
 * fantasma más rápido en llegar a cada nodo del pasillo.
 * margen = margenCorredor combinado con el mejor pasillo siguiente (lookahead).
 */
public class Ruta {

    MOVE movimiento;
    int[] nodos;
    int longitud;
    int fin;
    MOVE movFin;
    int margenCorredor;
    int margen;
    int pildoras;
    int posPoder = -1;
    int nodoPoder = -1;
    boolean tunel;
    boolean colgante;

    public MOVE getMovimiento() { return movimiento; }

    public int getLongitud() { return longitud; }

    /** Nodo donde termina el pasillo (normalmente un cruce). */
    public int getFin() { return fin; }

    public int getMargenCorredor() { return margenCorredor; }

    public int getMargen() { return margen; }

    /** Píldoras normales que se comen recorriendo el pasillo. */
    public int getPildoras() { return pildoras; }

    /** Paso (1..longitud) en el que se come una píldora de poder, o -1. */
    public int getPosPoder() { return posPoder; }

    public int getNodoPoder() { return nodoPoder; }

    public boolean isTunel() { return tunel; }

    public boolean isColgante() { return colgante; }

    public boolean esSegura() { return margen > Parametros.MARGEN_SEGURO; }

    /** Paso (1..longitud) en el que se pasa por el nodo, o -1. */
    public int posicionDe(int nodo) {
        for (int k = 0; k < longitud; k++)
            if (nodos[k] == nodo)
                return k + 1;
        return -1;
    }
}
