package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts;

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
	public static final int RADIO_PILL = 20;
 
	/** (*I) Distancia del fantasma a la pill amenazada para entrar en PELIGRO POR PÍLDORA. */
	public static final int RADIO_PELIGRO = 30;
 
	/** (*III) Holgura en "llega antes a la PP": empatar es lo peor que puede pasar. QUIZAS INTERESA QUE SEA 0 */
	public static final int MARGEN_PP = 4;
 
	/**
	 * Ventaja extra que se exige para pasar de HUIDA EFICIENTE a BLOQUEAR, y margen para
	 * salir de PELIGRO ("Ms. Pacman se aleja de la PP"), para no oscilar en el límite.
	 */
	public static final int HISTERESIS_PP = 3;
 
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
