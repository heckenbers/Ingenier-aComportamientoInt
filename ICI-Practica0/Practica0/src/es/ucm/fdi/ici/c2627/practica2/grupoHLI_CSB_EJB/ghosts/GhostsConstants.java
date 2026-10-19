package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts;

/**
 * Umbrales de la máquina de estados de los fantasmas, en un solo sitio.
 * Las referencias (*I), (*II)... son las notas del diagrama.
 * Todas las distancias se miden en nodos, por el laberinto.
 *
 * Los umbrales NO son final para que el Afinador pueda probar valores sin recompilar: Java
 * copia las constantes 'static final' dentro de cada clase que las usa, y cambiarlas en
 * ejecución no tendría efecto. Nadie más debe modificarlas. Cuando el afinado termine, se
 * escriben aquí los mejores valores (y, si se quiere, se vuelve a poner final).
 *
 * Valores actuales: resultado del Afinador (descenso por coordenadas contra los tres rivales).
 */
public final class GhostsConstants {

	private GhostsConstants() {}   // solo constantes: no se instancia

	// ---- Depuración ---------------------------------------------------------------

	/** Muestra cada máquina con GraphFSMObserver. OBLIGATORIO a false al entregar. */
	public static final boolean DEBUG = false;

	// ---- Reglas del motor -----------------------------------------------------------

	/** Un fantasma comestible avanza medio nodo por tick: tarda el doble en recorrer un tramo. */
	public static final int FACTOR_LENTITUD_COMESTIBLE = 2;

	// ---- PELIGRO POR PÍLDORA ------------------------------------------------------------

	/**
	 * Distancia de Ms. Pac-Man a una power pill activa para considerarla amenazada:
	 * la "pillAmenazada" de las notas (*I) y (*III).
	 */
	public static int RADIO_PILL = 35;

	/** (*I) Distancia del fantasma a la pill amenazada para entrar en PELIGRO POR PÍLDORA. */
	public static int RADIO_PELIGRO = 50;

	/**
	 * (*III) Holgura en "llega antes a la PP": distancia del fantasma + MARGEN_PP < la de Ms. Pac-Man.
	 * Con 0 basta con llegar antes, aunque sea por un turno.
	 */
	public static int MARGEN_PP = 0;

	/** Ventaja extra que se exige para pasar de HUIDA EFICIENTE a BLOQUEAR (no oscilar en el límite). */
	public static int HISTERESIS_BLOQUEO = 3;

	/**
	 * Un fantasma a esta distancia de la pill o menos ya la está guardando. Se mide sin tener
	 * en cuenta su dirección: al llegar a la pill no puede pararse y la pasa, pero sigue al lado.
	 */
	public static int RADIO_GUARDIA_PP = 2;

	/** Margen para salir de PELIGRO ("Ms. Pacman se aleja de la PP"), para no oscilar en el límite. */
	public static int HISTERESIS_PP = 3;

	// ---- NORMAL ---------------------------------------------------------------------------

	/**
	 * Nodos de ventaja que necesita un fantasma para quitarle el papel (perseguir, interceptar,
	 * vigilar) al que ya lo tiene. Evita que dos fantasmas casi a la misma distancia se
	 * intercambien el papel en cada cruce.
	 */
	public static int HISTERESIS_PAPELES = 3;

	/**
	 * Si Ms. Pac-Man está a esta distancia o menos de una power pill, se supone que va a por
	 * ella: el interceptor le corta el camino hacia la PP en vez de hacia su siguiente cruce.
	 */
	public static int RADIO_INTERCEPTAR_PP = 100;

	/** Turnos que el interceptor tiene que adelantarse a Ms. Pac-Man en un nodo para ir a cortarla allí. */
	public static int MARGEN_INTERCEPCION = 2;

	/**
	 * CERRAR PASO: el fantasma cierra el pasillo de Ms. Pac-Man si llega al cruce de salida
	 * cuando ella o antes: turnos del fantasma + MARGEN_CIERRE <= turnos de ella.
	 * Negativo = se permite llegar algo después (al llegar a EAT_DISTANCE también la mata).
	 */
	public static int MARGEN_CIERRE = 0;

	// ---- COMESTIBLE ----------------------------------------------------------------------

	/**
	 * (*II) Holgura en "fuera de peligro": tiempoComestible + MARGEN_HUIR < 2d/3.
	 * Si se cumple, aunque siga siendo comestible, Ms. Pac-Man ya no llega a comérselo.
	 */
	public static int MARGEN_HUIR = 4;

	/** Ms. Pac-Man "está cerca" de un comestible a esta distancia o menos (vuelve a HUIDA). */
	public static int DISTANCIA_CERCA_PACMAN = 30;

	/** Ms. Pac-Man "está lejos" a más de DISTANCIA_CERCA_PACMAN + este margen (para dispersarse). */
	public static int HISTERESIS_CERCA = 5;

	/** Radio alrededor de un compañero no comestible dentro del cual el comestible está protegido. */
	public static int RADIO_SEGURIDAD = 4;

	/** Holgura en "llega a compañero antes que Ms. Pacman": 2·(dC − RADIO_SEGURIDAD) + MARGEN < 2d/3. */
	public static int MARGEN_REFUGIO = 8;
}