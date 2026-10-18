package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts;

/**
 * Umbrales de la máquina de estados de los fantasmas, en un solo sitio.
 * Las referencias (*I), (*II)... son las notas del diagrama.
 * Todas las distancias se miden en nodos, por el laberinto.
 */
public final class GhostsConstants {

	private GhostsConstants() {}   // solo constantes: no se instancia

	// ---- Depuración ---------------------------------------------------------------

	/** Muestra cada máquina con GraphFSMObserver. OBLIGATORIO a false al entregar. */
	public static final boolean DEBUG = true;

	// ---- Reglas del motor -----------------------------------------------------------

	/** Un fantasma comestible avanza medio nodo por tick: tarda el doble en recorrer un tramo. */
	public static final int FACTOR_LENTITUD_COMESTIBLE = 2;

	// ---- PELIGRO POR PÍLDORA ------------------------------------------------------------

	/**
	 * Distancia de Ms. Pac-Man a una power pill activa para considerarla amenazada:
	 * la "pillAmenazada" de las notas (*I) y (*III).
	 */
	public static final int RADIO_PILL = 30;          // antes 20: la amenaza se detectaba tarde

	/** (*I) Distancia del fantasma a la pill amenazada para entrar en PELIGRO POR PÍLDORA. */
	public static final int RADIO_PELIGRO = 45;       // antes 30: huían cuando ya estaban demasiado cerca

	/**
	 * (*III) Holgura en "llega antes a la PP". Basta con no empatar: si el fantasma llega
	 * antes, aunque sea por poco, Ms. Pac-Man ya no puede comérsela sin chocar con él.
	 */
	public static final int MARGEN_PP = 1;            // antes 4: junto con la histeresis pedía 7 nodos de ventaja

	/** Ventaja extra que se exige para pasar de HUIDA EFICIENTE a BLOQUEAR (no oscilar en el límite). */
	public static final int HISTERESIS_BLOQUEO = 2;

	/**
	 * Un fantasma a esta distancia de la pill o menos ya la está guardando. Se mide sin tener
	 * en cuenta su dirección: al llegar a la pill no puede pararse y la pasa, pero sigue al lado.
	 */
	public static final int RADIO_GUARDIA_PP = 4;

	/** Margen para salir de PELIGRO ("Ms. Pacman se aleja de la PP"), para no oscilar en el límite. */
	public static final int HISTERESIS_PP = 3;

	// ---- NORMAL ---------------------------------------------------------------------------

	/**
	 * Nodos de ventaja que necesita un fantasma para quitarle el papel (perseguir, interceptar,
	 * vigilar) al que ya lo tiene. Evita que dos fantasmas casi a la misma distancia se
	 * intercambien el papel en cada cruce.
	 */
	public static final int HISTERESIS_PAPELES = 3;

	// ---- COMESTIBLE ----------------------------------------------------------------------

	/**
	 * (*II) Holgura en "fuera de peligro": tiempoComestible + MARGEN_HUIR < 2d/3.
	 * Si se cumple, aunque siga siendo comestible, Ms. Pac-Man ya no llega a comérselo.
	 */
	public static final int MARGEN_HUIR = 4;

	/** Ms. Pac-Man "está cerca" de un comestible a esta distancia o menos (vuelve a HUIDA). */
	public static final int DISTANCIA_CERCA_PACMAN = 20;

	/** Ms. Pac-Man "está lejos" a más de DISTANCIA_CERCA_PACMAN + este margen (para dispersarse). */
	public static final int HISTERESIS_CERCA = 5;

	/** Radio alrededor de un compañero no comestible dentro del cual el comestible está protegido. */
	public static final int RADIO_SEGURIDAD = 4;

	/** Holgura en "llega a compañero antes que Ms. Pacman": 2·(dC − RADIO_SEGURIDAD) + MARGEN < 2d/3. */
	public static final int MARGEN_REFUGIO = 4;
}