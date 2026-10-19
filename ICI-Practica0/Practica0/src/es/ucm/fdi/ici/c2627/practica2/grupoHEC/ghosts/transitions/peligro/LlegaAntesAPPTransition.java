package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * (*III) "Llega antes a la PP": distAPill + MARGEN_PP < distancia de Ms. Pac-Man a la pill.
 * Con negar = true es la flecha BLOQUEAR -> HUIDA, "Ya no llega antes a la PP".
 * (La entrada a BLOQUEAR es EsBloqueadorTransition, que exige además HISTERESIS_PP de
 * ventaja: entre los dos umbrales el fantasma no cambia de estado.)
 */
public class LlegaAntesAPPTransition implements Transition {

	private final GHOST ghost;
	private final boolean negar;
	private final String nombre;

	public LlegaAntesAPPTransition(GHOST ghost, boolean negar, String nombre) {
		this.ghost = ghost;
		this.negar = negar;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).llegaAntesAPill(ghost) != negar;
	}

	@Override
	public String toString() {
		return nombre;
	}
}