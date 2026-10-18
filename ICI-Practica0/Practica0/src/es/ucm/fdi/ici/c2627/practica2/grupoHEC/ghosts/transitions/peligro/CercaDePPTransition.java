package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * NORMAL -> PELIGRO POR PÍLDORA, "Ms. Pacman está cerca de PP" (*I): hay una power pill
 * amenazada (Ms. Pac-Man a RADIO_PILL o menos) y el fantasma está a RADIO_PELIGRO o menos de ella.
 */
public class CercaDePPTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public CercaDePPTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).cercaDePillAmenazada(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}