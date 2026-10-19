package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/** "Termina tiempo de cárcel": el fantasma ya está en el laberinto. Siempre sale no comestible. */
public class SaleDeCarcelTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public SaleDeCarcelTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return !((GhostsInput) in).enCarcel(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}