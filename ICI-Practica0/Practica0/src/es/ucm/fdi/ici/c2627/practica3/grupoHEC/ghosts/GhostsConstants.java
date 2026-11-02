package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts;

/**
 * Constantes de los CÁLCULOS en Java (Movimientos y GhostsInput).
 *
 * Los umbrales de DECISIÓN (radio de peligro, cerca/lejos, márgenes para bloquear...) ya no
 * van aquí: van en las reglas del .clp, para que cada versión pueda cambiarlos sin tocar Java.
 * Java calcula valores (distancias, márgenes) y las reglas los comparan.
 *
 * Son 'final' a propósito: la entrega no puede tener estado en atributos static.
 * Valores heredados de la práctica 2 (afinados allí).
 */
public final class GhostsConstants {

	private GhostsConstants() {}

	/** Un fantasma comestible avanza medio nodo por turno: tarda el doble en recorrer un tramo. */
	public static final int FACTOR_LENTITUD_COMESTIBLE = 2;

	/** Si Ms. Pac-Man está a esta distancia o menos de una PP, se supone que va a por ella. */
	public static final int RADIO_INTERCEPTAR_PP = 100;

	/** Turnos que el interceptor tiene que adelantarse a Ms. Pac-Man en un nodo para cortarla allí. */
	public static final int MARGEN_INTERCEPCION = 2;

	/** Holgura para que un comestible solo intercepte si el encuentro es cuando ya no lo es. */
	public static final int MARGEN_HUIR = 4;

	/** A esta distancia de la PP o menos, un fantasma ya la está guardando (se mide sin dirección). */
	public static final int RADIO_GUARDIA_PP = 2;

	/** Radio alrededor de un compañero no comestible dentro del cual el comestible está protegido. */
	public static final int RADIO_SEGURIDAD = 4;

	/** Holgura en "llega al compañero antes que Ms. Pac-Man a él": 2·(dC − RADIO_SEGURIDAD) + MARGEN < 2d/3. */
	public static final int MARGEN_REFUGIO = 8;

	/** Distancia que se usa cuando algo no existe o no se puede medir (fantasma en la cárcel, sin PP...). */
	public static final int LEJOS = 1000;
}