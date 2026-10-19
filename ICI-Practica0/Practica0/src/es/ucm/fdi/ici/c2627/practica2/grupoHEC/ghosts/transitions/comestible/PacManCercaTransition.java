package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.comestible;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/** DISPERSARSE -> HUIDA EFICIENTE, "Ms. Pacman está cerca": a DISTANCIA_CERCA_PACMAN o menos. */
public class PacManCercaTransition implements Transition {

	private final GHOST ghost;
	private final String nombre;

	public PacManCercaTransition(GHOST ghost, String nombre) {
		this.ghost = ghost;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).pacmanCerca(ghost);
	}

	@Override
	public String toString() {
		return nombre;
	}
}