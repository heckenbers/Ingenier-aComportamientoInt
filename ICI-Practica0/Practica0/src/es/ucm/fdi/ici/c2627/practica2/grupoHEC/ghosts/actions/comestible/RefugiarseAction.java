package es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.actions.comestible;

import java.util.function.Supplier;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.Reparto;
import es.ucm.fdi.ici.c2627.practica2.grupoHEC.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * REFUGIARSE EN COMPAÑERO (*IV): ir hacia el compañero no comestible que le asignó el
 * reparto. Si Ms. Pac-Man le sigue, se encuentra con un fantasma que sí puede comérsela.
 *
 * Las acciones solo reciben el Game, así que el compañero se lee del Reparto del turno a
 * través de un Supplier que le da el controlador. Si este turno no tiene compañero (la
 * transición de salida aún no se ha evaluado), huye para no quedarse sin decidir.
 */
public class RefugiarseAction implements Action {

	private final GHOST ghost;
	private final Supplier<Reparto> reparto;

	public RefugiarseAction(GHOST ghost, Supplier<Reparto> reparto) {
		this.ghost = ghost;
		this.reparto = reparto;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;

		GHOST companero = reparto.get().refugio(ghost);
		if (companero == null)
			return Movimientos.huidaEficiente(game, ghost,
					game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());

		return Movimientos.irHacia(game, ghost, game.getGhostCurrentNodeIndex(companero));
	}

	@Override
	public String getActionId() {
		return ghost + " refugiarse en compañero";
	}
}