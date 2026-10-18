package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.comestible;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * HUIDA EFICIENTE -> DISPERSARSE, "Ms. Pacman está lejos": a más de
 * DISTANCIA_CERCA_PACMAN + HISTERESIS_CERCA. Entre los dos umbrales no cambia de estado,
 * para no oscilar entre huir y dispersarse en cada cruce.
 */
public class PacManLejosTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public PacManLejosTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).pacmanLejos(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}