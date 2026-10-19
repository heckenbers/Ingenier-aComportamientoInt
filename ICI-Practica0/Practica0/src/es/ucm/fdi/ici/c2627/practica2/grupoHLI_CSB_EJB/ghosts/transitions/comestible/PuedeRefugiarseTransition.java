package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.comestible;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * (*IV) "Puede refugiarse": tiene un compañero no comestible al que llega antes de que
 * Ms. Pac-Man le alcance. Con negar = true es la flecha de vuelta, "No puede refugiarse".
 * Una sola clase para las dos flechas: así la condición y su negación no se separan nunca.
 */
public class PuedeRefugiarseTransition implements Transition {

	private final GHOST ghost;
	private final boolean negar;
	private final String nombre;

	public PuedeRefugiarseTransition(GHOST ghost, boolean negar, String nombre) {
		this.ghost = ghost;
		this.negar = negar;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).puedeRefugiarse(ghost) != negar;
	}

	@Override
	public String toString() {
		return nombre;
	}
}