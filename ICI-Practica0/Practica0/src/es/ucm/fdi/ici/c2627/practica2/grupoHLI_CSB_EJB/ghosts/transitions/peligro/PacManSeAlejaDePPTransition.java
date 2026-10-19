package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.transitions.peligro;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.GhostsInput;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * PELIGRO POR PÍLDORA -> NORMAL, "Ms. Pacman se aleja de la PP": está a más de
 * RADIO_PILL + HISTERESIS_PP de la pill (o ya no quedan). El margen evita entrar y salir
 * de PELIGRO en cada turno si Ms. Pac-Man se queda justo en el límite.
 *
 * No recibe el fantasma: la condición solo depende de Ms. Pac-Man y la pill. Aun así,
 * cada máquina necesita su propia instancia (una flecha = un objeto).
 */
public class PacManSeAlejaDePPTransition implements Transition {

	private final String nombre;

	public PacManSeAlejaDePPTransition(String nombre) {
		this.nombre = nombre;
	}

	@Override
	public boolean evaluate(Input in) {
		return ((GhostsInput) in).pacmanLejosDePill();
	}

	@Override
	public String toString() {
		return nombre;
	}
}