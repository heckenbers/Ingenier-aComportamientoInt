package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.normal;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto.Papel;
import es.ucm.fdi.ici.fsm.Transition;
import pacman.game.Constants.GHOST;

/**
 * (*V) "Le toca el papel X": el reparto de este turno (GhostsInput) le ha dado ese papel.
 * Todas las flechas de NORMAL son de esta clase con distinto papel: quién hace qué lo
 * decide el reparto, con prioridades e histéresis, y la máquina solo lo sigue.
 */
public class TienePapelTransition implements Transition {

	private final GHOST ghost;
	private final Papel papel;
	private final String nombre;

	public TienePapelTransition(GHOST ghost, Papel papel, String nombre) {
		this.ghost = ghost;
		this.papel = papel;
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).papel(ghost) == papel;
	}

	@Override
	public String toString() {
		return nombre;
	}
}