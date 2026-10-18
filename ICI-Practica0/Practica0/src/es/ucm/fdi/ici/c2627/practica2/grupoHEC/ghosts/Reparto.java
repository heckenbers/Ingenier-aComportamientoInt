package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts;

import java.util.EnumMap;

import pacman.game.Constants.GHOST;

/**
 * Reparto del equipo en un turno: el papel de cada fantasma en NORMAL, la esquina de cada
 * comestible y el compañero en el que se refugia. Lo calcula GhostsInput y lo guarda el
 * controlador para dos cosas:
 *  - pasárselo al GhostsInput del turno siguiente (histéresis: el que tiene un papel lo conserva);
 *  - que las acciones sepan su destino (las acciones solo reciben el Game, no el Input).
 */
public class Reparto {

	/** Papeles de NORMAL, por orden de prioridad (nota *V del diagrama). */
	public enum Papel { PERSEGUIR, INTERCEPTAR, VIGILAR, PATRULLAR, NINGUNO }

	private final EnumMap<GHOST, Papel> papeles = new EnumMap<>(GHOST.class);
	private final EnumMap<GHOST, Integer> esquinas = new EnumMap<>(GHOST.class);
	private final EnumMap<GHOST, GHOST> refugios = new EnumMap<>(GHOST.class);

	/** Papel del fantasma en NORMAL, o NINGUNO si no está en NORMAL. */
	public Papel papel(GHOST g) {
		return papeles.getOrDefault(g, Papel.NINGUNO);
	}

	/** Fantasma que tiene un papel, o null si nadie lo tiene. */
	public GHOST quienTiene(Papel p) {
		for (GHOST g : GHOST.values())
			if (papeles.get(g) == p) return g;
		return null;
	}

	/** Nodo de la esquina asignada a un comestible, o -1 si no tiene. */
	public int esquina(GHOST g) {
		return esquinas.getOrDefault(g, -1);
	}

	/** Compañero no comestible en el que se refugia, o null si no tiene. */
	public GHOST refugio(GHOST g) {
		return refugios.get(g);
	}

	// ---- Solo los usa GhostsInput al construir el reparto ----------------------------

	void asignarPapel(GHOST g, Papel p) { papeles.put(g, p); }

	void asignarEsquina(GHOST g, int nodo) { esquinas.put(g, nodo); }

	void asignarRefugio(GHOST g, GHOST companero) { refugios.put(g, companero); }

	boolean esquinaOcupada(int nodo) { return esquinas.containsValue(nodo); }
}