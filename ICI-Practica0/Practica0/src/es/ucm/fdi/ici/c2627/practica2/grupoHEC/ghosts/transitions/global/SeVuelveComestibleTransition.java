package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * NORMAL -> COMESTIBLE, "Ms. Pacman se come PP": el fantasma es comestible y todavía está
 * en peligro (no cumple *II). Es justo la negación de VuelveANormalTransition fuera de la
 * cárcel, así que las dos flechas nunca se activan a la vez.
 */
public class SeVuelveComestibleTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public SeVuelveComestibleTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).enPeligroComestible(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}