package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB;

/**
 * Umbrales de la máquina de estados de Ms. Pac-Man, todos en un solo sitio.
 * Las distancias están en nodos y se miden por los pasillos (DM.PATH).
 */
public final class Constantes {

    private Constantes() {
    }
    
    /** Depuración de las máquinas: true abre la ventana con los grafos y escribe en consola cada transición. Debe estar a false al entregar. */
    public static final boolean DEBUG =true;

    /** Distancia que se usa cuando no hay ningún fantasma (o píldora) que medir. */
    public static final int SIN_DISTANCIA = Integer.MAX_VALUE;

    /** Distancia que se le asigna a una píldora a la que no hay camino, para que nunca gane una comparación. */
    public static final int DISTANCIA_PILDORA_INALCANZABLE = 100000;

    /** Margen de histéresis: diferencia entre el umbral de entrada y el de salida de un estado. */
    public static final int HISTERESIS = 5;

    // ------------------------------------------------------------------ Huir

    /** Huir: se entra si el fantasma no comestible más cercano está a menos de estos nodos. */
    public static final int HUIR_ENTRADA = 25;
    /** Huir: se sigue huyendo mientras ese fantasma esté a menos de estos nodos (se sale a 30 o más). */
    public static final int HUIR_SALIDA = HUIR_ENTRADA + HISTERESIS;

    // ---------------------------------------------------------- Comer fantasma

    /** Comer fantasma: se entra si el fantasma comestible más cercano está a menos de estos nodos. */
    public static final int CAZAR_ENTRADA = 40;
    /** Comer fantasma: se sigue cazando mientras esté a estos nodos o menos (se sale a más de 45). */
    public static final int CAZAR_SALIDA = CAZAR_ENTRADA + HISTERESIS;

    // ------------------------------------------------------ Píldora de poder

    /** Parámetro "limit": se va a la píldora de poder si está a este número de nodos o menos. */
    public static final int PODER_LIMITE = 15;
    /** Ir a la píldora de poder: se sigue yendo mientras esté a estos nodos o menos (limit + histéresis). */
    public static final int PODER_SALIDA = PODER_LIMITE + HISTERESIS;

    // --------------------------------------------------------- Evitar camino

    /** Evitar camino: se entra si el fantasma que corta la ruta está a menos de estos nodos de la píldora de poder. */
    public static final int EVITAR_ENTRADA = 12;
    /** Evitar camino: se sigue evitando mientras algún fantasma esté a estos nodos o menos de la píldora (se sale a más de 17). */
    public static final int EVITAR_SALIDA = EVITAR_ENTRADA + HISTERESIS;
    /** Un fantasma corta una ruta si llega a un nodo de ella con esta ventaja (nodos) respecto a Ms. Pac-Man. */
    public static final int MARGEN_INTERCEPCION = 2;
    /** Cuántos nodos de una ruta se revisan al buscar fantasmas que la corten (más allá ya no es fiable). */
    public static final int NODOS_REVISADOS_RUTA = 25;
    /** Cuántos cruces candidatos se prueban como desvío antes de rendirse y elegir un movimiento seguro. */
    public static final int NUM_CANDIDATOS_DESVIO = 4;

    // ---------------------------------------------------------------- Patrullar

    /** Patrullar: se entra si hay un fantasma peligroso a menos de estos nodos de la píldora común más cercana. */
    public static final int PATRULLA_ENTRADA = 5;
    /** Patrullar: se sigue patrullando mientras haya un fantasma a menos de estos nodos de esa píldora. */
    public static final int PATRULLA_SALIDA = PATRULLA_ENTRADA + HISTERESIS;

    /** Patrullar: un cruce es seguro si Ms. Pac-Man llega a él con al menos estos nodos de ventaja sobre todos los fantasmas. */
    public static final int PATRULLA_VENTAJA = 2;

    // ---------------------------------------------------------------- Esquivar píldora de poder

    /** Esquivar: se entra si hay una píldora de poder a estos nodos o menos y no hay fantasma comestible que cazar. */
    public static final int ESQUIVAR_ENTRADA = 10;
    /** Esquivar: se sigue esquivando mientras la píldora de poder esté a estos nodos o menos. */
    public static final int ESQUIVAR_SALIDA = ESQUIVAR_ENTRADA + HISTERESIS;

    // ---------------------------------------------------------------- Limpiar últimas

    /** Limpiar últimas: se entra si quedan menos de estas píldoras comunes. */
    public static final int LIMPIAR_ENTRADA = 20;
    /** Limpiar últimas: se sigue limpiando mientras queden menos de estas píldoras comunes. */
    public static final int LIMPIAR_SALIDA = LIMPIAR_ENTRADA + HISTERESIS;

    // ---------------------------------------------------------------- Esperar junto a la píldora de poder

    /** Esperar: se empieza a esperar si Ms. Pac-Man está a estos nodos o menos de la píldora de poder. */
    public static final int ESPERA_JUNTO = 8;
    /** Esperar: radio máximo (nodos) al que se aleja de la píldora de poder mientras espera. */
    public static final int ESPERA_RADIO = 12;
    /** Esperar: cuánto cuenta (en unidades de PESO_SEGURIDAD) salirse del radio; mayor que DISTANCIA_SEGURA para que domine. */
    public static final int PENALIZACION_FUERA_DE_RADIO = 100;
    /** Esperar: se come la píldora cuando hay al menos estos fantasmas peligrosos cerca (ver ESPERA_DISTANCIA). */
    public static final int ESPERA_FANTASMAS = 2;
    /** Esperar: distancia (nodos) a Ms. Pac-Man a la que un fantasma peligroso cuenta como cerca. */
    public static final int ESPERA_DISTANCIA = 30;
    /** Esperar: turnos máximos esperando antes de ir a por la píldora de todas formas. */
    public static final int ESPERA_MAX_TURNOS = 60;

    // ---------------------------------------------------------------- Huir hacia la píldora de poder

    /** Huir a la píldora: se entra si Ms. Pac-Man llega a ella con más de estos nodos de ventaja sobre los fantasmas. */
    public static final int HUIR_PODER_ENTRADA = 5;
    /** Huir a la píldora: se sigue yendo mientras la ventaja sea mayor que estos nodos (salida con histéresis). */
    public static final int HUIR_PODER_SALIDA = 0;
    /** Huir a la píldora: solo si está a estos nodos o menos de Ms. Pac-Man. */
    public static final int HUIR_PODER_ALCANCE = 40;
    /** Huir a la píldora: ningún fantasma puede estar a menos de estos nodos de la ruta. */
    public static final int HUIR_PODER_DISTANCIA_RUTA = 6;

    // -------------------------------------------------------- Movimiento seguro

    /** Distancia a los fantasmas a partir de la cual dos movimientos se consideran igual de seguros. */
    public static final int DISTANCIA_SEGURA = HUIR_SALIDA;
    /** Peso de la seguridad (cada nodo de distancia a un fantasma) frente a acercarse a la píldora (cada nodo cuenta 1). */
    public static final long PESO_SEGURIDAD = 100000L;

    // -------------------------------------------------------- Comer píldora

    /** Cuántas píldoras (las más cercanas) se prueban como objetivo antes de rendirse con un movimiento seguro. */
    public static final int NUM_CANDIDATAS_PILDORA = 5;
}