package es.ucm.fdi.ici.c2627.practica0.grupoIndividual;

/**
 * Todos los umbrales de los dos controladores, en un solo sitio.
 * Se afinan jugando partidas (runExperiment), de uno en uno.
 */
public class Constantes {

    // ------------------------------------------- Ms. Pac-Man: mapa de influencia

    /** Lo que queda de una fuente tras cada paso por el pasillo (alcance ~ ln 0,1 / ln caída). */
    public static final double CAIDA_COMIDA = 0.90;
    public static final double CAIDA_AMENAZA = 0.90;
    /** Más corta que la amenaza: un comestible lejano no se alcanza antes de que deje de serlo. */
    public static final double CAIDA_PRESA = 0.85;
    public static final double CAIDA_PODER = 0.90;

    /** Pesos de la función objetivo U(n). El signo con el que entra cada capa se pone en utilidad(). */
    public static final double PESO_COMIDA = 1.0;
    public static final double PESO_AMENAZA = 6.0;
    public static final double PESO_PRESA = 2.0;
    /** Peso con el que atrae la píldora de poder cuando hay fantasmas encima. */
    public static final double PESO_PODER_ATRAE = 4.0;
    /** Peso con el que repele (no gastarla de paso) cuando no hay presión. */
    public static final double PESO_PODER_EVITA = 2.0;

    /** Distancia (en nodos, por pasillo) a la que un fantasma cuenta como "cerca". */
    public static final int CERCA = 30;
    /** Cuántos fantasmas cerca hacen falta para que la píldora de poder pase a atraer. */
    public static final int FANTASMAS_PARA_PILDORA = 2;
    /** Turnos sin comer nada tras los cuales se permite gastar la píldora de poder (final de nivel). */
    public static final int TURNOS_ESTANCADO = 120;
    /** Margen en la cuenta "2d + margen < tiempo comestible" para considerar alcanzable a un fantasma. */
    public static final int MARGEN_CAZA = 6;

    // --------------------------------------------------------------- Fantasmas

    /** Tope de nodos al recorrer un pasillo (evita bucles infinitos). */
    public static final int MAX_TRAMO = 300;
    /** Turnos de nivel durante los que los fantasmas se dispersan a sus esquinas. */
    public static final int TURNOS_DISPERSION = 80;
    /** Si Ms. Pac-Man está más cerca que esto, no se dispersa nadie. */
    public static final int DIST_FIN_DISPERSION = 25;
    /** Distancia de Ms. Pac-Man a una píldora de poder activa a la que los fantasmas reaccionan. */
    public static final int ALERTA_PODER = 12;
    /** Peso de separarse de otros fantasmas comestibles al huir (no huir en manada). */
    public static final double PESO_SEPARACION = 0.3;
    /** Probabilidad de movimiento aleatorio de Sue (más de un 5 % ya es jugar mal). */
    public static final double RUIDO = 0.05;

    // ---------------------------------------------------------------- Depurar

    public static final boolean DEBUG_PACMAN = false;
    public static final boolean DEBUG_GHOSTS = false;
}