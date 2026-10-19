package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * HUIDA EFICIENTE -> BLOQUEAR POWER PILL, "Llega antes a la PP" (*III) con ventaja extra
 * (HISTERESIS_PP) y siendo el fantasma no comestible más cercano a la pill. Así no se
 * lanzan todos a bloquear a la vez.
 */
public class PuedeBloquearPPTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public PuedeBloquearPPTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).esBloqueador(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}