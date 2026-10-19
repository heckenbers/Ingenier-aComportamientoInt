package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.global;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * "Fantasma es comido": el fantasma está en la cárcel. Cubre también la muerte de
 * Ms. Pac-Man y el cambio de nivel, que devuelven a todos los fantasmas a la cárcel.
 * El nombre es un parámetro porque esta condición sale de varios superestados y cada
 * flecha de una misma máquina necesita un nombre distinto.
 */
public class EncerradoTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public EncerradoTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).enCarcel(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}