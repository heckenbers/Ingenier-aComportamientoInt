package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * COMESTIBLE -> NORMAL, "Deja de ser comestible o está fuera de peligro (*II)".
 *
 * Se exige además estar fuera de la cárcel: un fantasma recién comido tampoco es comestible,
 * y sin esta comprobación esta flecha competiría con "Fantasma es comido".
 */
public class VuelveANormalTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public VuelveANormalTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		GhostsInput input = (GhostsInput) in;
		return !input.enCarcel(ghost) && input.puedeVolverANormal(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}