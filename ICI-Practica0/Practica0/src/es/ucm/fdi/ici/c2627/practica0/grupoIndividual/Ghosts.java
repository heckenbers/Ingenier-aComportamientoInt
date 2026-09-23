package es.ucm.fdi.ici.c2627.practica0.grupoIndividual;

import java.util.EnumMap;
import java.util.Random;

import pacman.controllers.GhostController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts extends GhostController {
	private EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
	private MOVE[] allMoves = MOVE.values();
    private Random rnd = new Random();
	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		
		int limit = 10;
		for(GHOST ghostype : GHOST.values()) {
			if(!game.doesGhostRequireAction(ghostype)) continue;
			double pacman = game.getPacmanCurrentNodeIndex();
			if(game.isGhostEdible(ghostype) || (game.getDistance(game.getPacmanCurrentNodeIndex(), game.getClosestNodeIndexFromNodeIndex(game.getPacmanCurrentNodeIndex(), game.getActivePowerPillsIndices(), Constants.DM.EUCLID), Constants.DM.EUCLID) <= limit)) {
				moves.put(ghostype, game.getApproximateNextMoveAwayFromTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghostype), game.getGhostLastMoveMade(ghostype), Constants.DM.EUCLID));
			}
			else {
				Random rand = new Random();
				double random = rand.nextDouble();
				if(random < 0.9) {
					moves.put(ghostype, game.getApproximateNextMoveTowardsTarget(game.getPacmanCurrentNodeIndex(), game.getGhostCurrentNodeIndex(ghostype), game.getGhostLastMoveMade(ghostype), Constants.DM.EUCLID));
					
				}
				else {
					moves.put(ghostype, allMoves[rnd.nextInt(allMoves.length)]);
				}
					
			}
			
		}
		return null;
	}

}
