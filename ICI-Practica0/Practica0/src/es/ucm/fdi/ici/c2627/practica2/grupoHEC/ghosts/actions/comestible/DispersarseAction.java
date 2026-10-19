package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.comestible;

import java.util.function.Supplier;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * DISPERSARSE A ESQUINAS: con Ms. Pac-Man lejos, cada comestible va a una esquina distinta
 * (la que le asignó el reparto), para que no pueda comérselos en cadena si se acerca.
 * Si no tiene esquina asignada, huye.
 */
public class DispersarseAction implements Action {

	private final GHOST ghost;
	private final Supplier<Reparto> reparto;

	public DispersarseAction(GHOST ghost, Supplier<Reparto> reparto) {
		this.ghost = ghost;
		this.reparto = reparto;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		int esquina = reparto.get().esquina(ghost);
		if (esquina < 0)
			return Movimientos.huidaEficiente(game, ghost,
					game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());

		return Movimientos.irHacia(game, ghost, esquina);
	}

	@Override
	public String getActionId() {
		return ghost + " dispersarse a esquina";
	}
}