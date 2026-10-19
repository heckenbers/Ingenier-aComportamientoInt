package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * PELIGRO POR PÍLDORA -> COMESTIBLE, "Ms. Pacman se come la PP": el fantasma es comestible.
 *
 * No se reutiliza SeVuelveComestibleTransition (comestible Y en peligro) a propósito: si el
 * fantasma ya estuviera fuera de peligro y hubiera otra pill cerca de Ms. Pac-Man, ninguna
 * flecha de PELIGRO se activaría y se quedaría bloqueando siendo comestible. Pasando a
 * COMESTIBLE con cualquier comestible, la flecha COMESTIBLE -> NORMAL (*II) decide el turno siguiente.
 */
public class SeComePPTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public SeComePPTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).esComestible(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}