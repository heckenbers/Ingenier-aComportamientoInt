package es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.actions;

import es.ucm.fdi.ici.c2627.practica3.grupoHEC.ghosts.utils.Movimientos;
import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * REFUGIARSE EN COMPAÑERO (comestible): ir hacia un compañero no comestible. Acción con
 * parámetro: el compañero lo elige la regla y llega en el slot 'companero' del ACTION ganador
 * (lo calcula GhostsInput en el hecho FANTASMA). Si no hay compañero, huye.
 */
public class RefugiarseAction implements RulesAction {

	private final GHOST ghost;

	/** Compañero elegido por la regla en este turno (null si ninguno). */
	private GHOST companero;

	public RefugiarseAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		if (companero == null || game.getGhostLairTime(companero) > 0)
			return Movimientos.huidaEficiente(game, ghost,
					game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
		return Movimientos.irHacia(game, ghost, game.getGhostCurrentNodeIndex(companero));
	}

	/** Lee el compañero del ACTION ganador: (ACTION (id refugiarse) (companero PINKY) ...). */
	@Override
	public void parseFact(Fact actionFact) {
		try {
			companero = GHOST.valueOf(actionFact.getSlotValue("companero").symbolValue(null));
		} catch (Exception e) {      // NINGUNO, slot vacío o error de Jess
			companero = null;
		}
	}

	@Override
	public String getActionId() {
		return "refugiarse";
	}
}