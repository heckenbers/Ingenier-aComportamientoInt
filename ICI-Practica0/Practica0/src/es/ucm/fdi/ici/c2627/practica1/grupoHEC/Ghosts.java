package es.ucm.fdi.ici.c2627.practica1.grupoHEC;

import java.util.EnumMap;
import java.util.Random;

import pacman.controllers.GhostController;
import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

public class Ghosts extends GhostController {

	private static final int LIMIT = 10;              // distancia de MsPacMan a una power pill para empezar a huir
	private static final double CHASE_PROB = 0.9;     // probabilidad de perseguir (el resto, movimiento aleatorio)

	private EnumMap<GHOST, MOVE> moves = new EnumMap<GHOST, MOVE>(GHOST.class);
	private Random rnd = new Random();

	@Override
	public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
		// Limpiar las órdenes del tick anterior para no reenviar movimientos viejos
		moves.clear();

		int pacmanNode = game.getPacmanCurrentNodeIndex();

		// Se calcula una vez por tick: es igual para todos los fantasmas
		boolean pacmanNearPowerPill = isPacmanNearPowerPill(game, pacmanNode);

		for (GHOST ghostType : GHOST.values()) {
			// Solo decidimos en intersecciones (y nunca dentro de la casa)
			if (!game.doesGhostRequireAction(ghostType)) continue;

			int ghostNode = game.getGhostCurrentNodeIndex(ghostType);
			MOVE lastMove = game.getGhostLastMoveMade(ghostType);

			if (game.isGhostEdible(ghostType) || pacmanNearPowerPill) {
				// HUIR: desde el fantasma, alejándose de MsPacMan
				moves.put(ghostType, game.getApproximateNextMoveAwayFromTarget(
						ghostNode, pacmanNode, lastMove, Constants.DM.EUCLID));
			}
			else if (rnd.nextDouble() < CHASE_PROB) {
				// PERSEGUIR: desde el fantasma, hacia MsPacMan
				moves.put(ghostType, game.getApproximateNextMoveTowardsTarget(
						ghostNode, pacmanNode, lastMove, Constants.DM.EUCLID));
			}
			else {
				// ALEATORIO: solo entre los movimientos válidos desde este nodo
				MOVE[] possibleMoves = game.getPossibleMoves(ghostNode, lastMove);
				moves.put(ghostType, possibleMoves[rnd.nextInt(possibleMoves.length)]);
			}
		}

		return moves;
	}

	// Devuelve true si MsPacMan está a LIMIT o menos de la power pill activa más cercana
	private boolean isPacmanNearPowerPill(Game game, int pacmanNode) {
		int[] powerPills = game.getActivePowerPillsIndices();
		if (powerPills.length == 0) return false;   // evita el índice -1 cuando no quedan power pills

		int closest = game.getClosestNodeIndexFromNodeIndex(pacmanNode, powerPills, Constants.DM.EUCLID);
		return game.getDistance(pacmanNode, closest, Constants.DM.EUCLID) <= LIMIT;
	}
}