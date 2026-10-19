package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman;

import static pacman.game.Constants.EAT_DISTANCE;

/**
 * Parámetros de la HFSM. Todos están en ticks o son relativos al grafo
 * (diámetro, radio), así que ninguno depende de un laberinto concreto.
 * Se deberían calibrar simulando contra varios tipos de fantasmas.
 */
public final class Parametros {

    private Parametros() {}

    // ---------- seguridad (ticks) ----------
    /** Si el fantasma activo más cercano llega a Pac-Man en menos de esto: peligro. */
    public static final int TAU_ALERTA = 12;
    /** Si llega en menos de esto: crítico. */
    public static final int TAU_CRIT = 6;
    /** Ventaja mínima (ticks) sobre los fantasmas para considerar segura una ruta. */
    public static final int MARGEN_SEGURO = EAT_DISTANCE + 2;
    /** Margen que se asigna a una ruta que llega a una píldora de poder antes que nadie. */
    public static final int MARGEN_INFINITO = 1000;
    /** Ticks seguidos sin peligro para volver de SUPERVIVENCIA a RECOLECTAR. */
    public static final int HISTERESIS = 3;

    // ---------- exploración de rutas ----------
    /** Pasillos encadenados que se miran por delante (lookahead). */
    public static final int PROFUNDIDAD = 3;
    /** Longitud máxima de un pasillo recorrido de una vez. */
    public static final int MAX_CORREDOR = 60;

    // ---------- poder y caza ----------
    /** Ticks de holgura al decidir si un fantasma comestible es alcanzable. */
    public static final int HOLGURA_CAZA = 4;
    /** poder_rentable si T_susto >= velocidad * FACTOR_TAU_MIN * radio. */
    public static final double FACTOR_TAU_MIN = 0.25;
    /** valor_cadena mínimo para entrar en PrepararCebo (2 fantasmas: 200 + 400). */
    public static final int VALOR_MIN_CEBO = 600;
    /** valor_cadena con el que se come el cebo sin esperar más (3 fantasmas). */
    public static final int UMBRAL_CADENA = 1400;
    /** Distancia máxima al cebo, como fracción del diámetro del laberinto. */
    public static final double DIST_MAX_CEBO = 0.35;
    /** Distancia a la que se patrulla alrededor de la píldora de poder. */
    public static final int RADIO_PATRULLA = 6;
    /** Ticks esperando en el cebo antes de comérselo con lo que haya. */
    public static final int PACIENCIA_CEBO = 80;
    /** Ticks sin volver a intentar un cebo tras abandonar uno. */
    public static final int ENFRIAMIENTO_CEBO = 150;

    // ---------- recolección ----------
    /** Dos píldoras a esta distancia o menos pertenecen al mismo clúster. */
    public static final int ENLACE_CLUSTER = 4;
    /** Tamaño máximo de un clúster para que se considere aislado. */
    public static final int MAX_TAM_AISLADO = 10;
    /** Un clúster es aislado si está a más de esta fracción del diámetro del resto. */
    public static final double FRACCION_AISLADO = 0.15;
    /** Ventaja necesaria para desviarse a limpiar un clúster aislado. */
    public static final int MARGEN_HOLGADO = 2 * TAU_ALERTA;
    /** "Quedan pocas píldoras" si quedan menos de esta fracción... */
    public static final double FRACCION_POCAS = 0.1;
    /** ...o menos de este número. */
    public static final int MIN_POCAS = 8;

    // ---------- preproceso ----------
    /** Radio (en pasos) con el que se mide escape(n). */
    public static final int RADIO_ESCAPE = 8;

    // ---------- penalizaciones de puntuación ----------
    public static final double PENAL_PODER = 500;
    public static final double PENAL_COLGANTE = 200;
}
