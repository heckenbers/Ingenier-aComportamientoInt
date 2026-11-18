package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.actions;

import static es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.GhostsConstants.*;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.ghosts.utils.Movimientos;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * REFUGIARSE EN COMPAÑERO (comestible): va hacia el compañero no comestible más cercano al que
 * llega antes de que Ms. Pac-Man lo alcance. Si no hay ninguno válido, hace huida eficiente.
 */
public class RefugiarseAction implements Action {

	private final GHOST ghost;

	public RefugiarseAction(GHOST ghost) {
		this.ghost = ghost;
	}

	@Override
	public MOVE execute(Game game) {
		if (!game.doesGhostRequireAction(ghost)) return MOVE.NEUTRAL;
		GHOST companero = buscarCompanero(game);
		if (companero == null || game.getGhostLairTime(companero) > 0)
			return Movimientos.huidaEficiente(game, ghost, game.getPacmanCurrentNodeIndex(), game.getPacmanLastMoveMade());
		return Movimientos.irHacia(game, ghost, game.getGhostCurrentNodeIndex(companero));
	}

	private GHOST buscarCompanero(Game game) {
		int miNodo = game.getGhostCurrentNodeIndex(ghost);
		MOVE miUltimo = game.getGhostLastMoveMade(ghost);
		int distPacman = Movimientos.distanciaFantasma(game, ghost, game.getPacmanCurrentNodeIndex());
		int ellaAMi = game.getShortestPathDistance(game.getPacmanCurrentNodeIndex(), miNodo, game.getPacmanLastMoveMade());
		int distPeligro = Math.min(ellaAMi, distPacman);

		GHOST mejor = null;
		int mejorDist = Integer.MAX_VALUE;
		for (GHOST c : GHOST.values()) {
			if (c == ghost || game.getGhostLairTime(c) > 0 || game.isGhostEdible(c)) continue;
			int nodoC = game.getGhostCurrentNodeIndex(c);
			int dC = game.getShortestPathDistance(miNodo, nodoC, miUltimo);
			long tiempoYo = (long) FACTOR_LENTITUD_COMESTIBLE * Math.max(0, dC - RADIO_SEGURIDAD) + MARGEN_REFUGIO;
			boolean ellaEnMedio = Movimientos.distanciaPacman(game, nodoC) <= dC;
			if (!ellaEnMedio && 3L * tiempoYo < 2L * distPeligro && dC < mejorDist) {
				mejor = c;
				mejorDist = dC;
			}
		}
		return mejor;
	}

	@Override
	public String getActionId() {
		return "refugiarse";
	}

	@Override
	public String toString() {
		return getActionId();
	}
}