package es.ucm.fdi.ici.c2627.practica0.grupoIndividual;

import java.util.ArrayList;
import java.util.EnumMap;

import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Game;

public class MsPacMan extends PacmanController {
	

	public GHOST getNearestChasingGhost(Game game, int limit) {
		GHOST result = null;
		double min = limit;
		for(GHOST ghostype: GHOST.values()) {
			double aux = game.getDistance(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghostype), game.getPacmanLastMoveMade(), Constants.DM.EUCLID);
			if(aux < limit) {
				if(aux < min) {
					result = ghostype;
					min = aux;
				}
			}
		}
		return result;
	}
	
	
	@Override
	public MOVE getMove(Game game, long timeDue) {
		// TODO Auto-generated method stub
		int limit = 20;
		GHOST nearestGhost = getNearestChasingGhost(game, limit);
		if(nearestGhost != null) {
			if(!game.isGhostEdible(nearestGhost)) {
				return game.getApproximateNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(nearestGhost), game.getPacmanLastMoveMade(), Constants.DM.EUCLID);
			}
			else game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(nearestGhost), game.getPacmanLastMoveMade(), Constants.DM.EUCLID);
		}
		return game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(), game.getActivePillsIndices(), Constants.DM.EUCLID), game.getPacmanLastMoveMade(), Constants.DM.EUCLID);
		
	}
	
	

}
