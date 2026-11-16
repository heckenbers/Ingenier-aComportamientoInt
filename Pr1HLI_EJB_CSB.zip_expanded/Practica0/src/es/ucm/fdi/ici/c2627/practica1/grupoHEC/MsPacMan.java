package es.ucm.fdi.ici.c2627.practica1.grupoHEC;

import java.awt.Color;

import pacman.controllers.PacmanController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MsPacMan extends PacmanController {

	private static final int LIMIT = 20;   // distancia (por el laberinto) a la que un fantasma se considera cercano
	private Color[] colours = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	@Override
	public MOVE getMove(Game game, long timeDue) {
		int pacmanNode = game.getPacmanCurrentNodeIndex();
		MOVE pacmanLastMove = game.getPacmanLastMoveMade();   // el del juego, no el campo heredado lastMove

		// -- VISUALIZAR PÍLDORAS DE PODER Y CAMINOS DE LOS FANTASMAS
		for (int powerPill : game.getActivePowerPillsIndices())
			GameView.addLines(game, Color.CYAN, pacmanNode, powerPill);

		for (GHOST g : GHOST.values()) {
			if (game.getGhostLairTime(g) > 0) continue;   // en la casa no hay camino
			GameView.addPoints(game, colours[g.ordinal()],
					game.getShortestPath(game.getGhostCurrentNodeIndex(g), pacmanNode, game.getGhostLastMoveMade(g)));
		}

		// -- COMPORTAMIENTO
		GHOST nearestGhost = getNearestGhost(game, pacmanNode, pacmanLastMove);
		if (nearestGhost != null) {
			// Solo se pide el nodo del fantasma después de comprobar que existe
			int ghostNode = game.getGhostCurrentNodeIndex(nearestGhost);
			if (!game.isGhostEdible(nearestGhost))
				return bestMove(game, pacmanNode, pacmanLastMove, ghostNode, false);   // huir
			else
				return bestMove(game, pacmanNode, pacmanLastMove, ghostNode, true);    // perseguir
		}

		// Píldora activa más cercana (las ya comidas no cuentan)
		int target = getClosestNode(game, pacmanNode, pacmanLastMove, game.getActivePillsIndices());
		if (target == -1)   // solo quedan power pills: sin esto no habría destino
			target = getClosestNode(game, pacmanNode, pacmanLastMove, game.getActivePowerPillsIndices());
		if (target == -1) return pacmanLastMove;

		return bestMove(game, pacmanNode, pacmanLastMove, target, true);
	}

	// Fantasma más cercano por el laberinto a menos de LIMIT, ignorando los que están en la casa.
	// Devuelve null si no hay ninguno.
	private GHOST getNearestGhost(Game game, int pacmanNode, MOVE pacmanLastMove) {
		GHOST result = null;
		int min = LIMIT;
		for (GHOST g : GHOST.values()) {
			if (game.getGhostLairTime(g) > 0) continue;   // encerrado: no es una amenaza
			int d = game.getShortestPathDistance(pacmanNode, game.getGhostCurrentNodeIndex(g), pacmanLastMove);
			if (d < min) {
				result = g;
				min = d;
			}
		}
		return result;
	}

	// Nodo de 'targets' más cercano por el laberinto, sin poder dar media vuelta. -1 si no hay ninguno.
	private int getClosestNode(Game game, int from, MOVE lastMove, int[] targets) {
		int best = -1;
		int min = Integer.MAX_VALUE;
		for (int t : targets) {
			int d = game.getShortestPathDistance(from, t, lastMove);
			if (d < min) {
				min = d;
				best = t;
			}
		}
		return best;
	}

	// Entre las salidas legales (sin media vuelta), la que deja más cerca (towards) o más lejos (!towards)
	// del objetivo. La distancia se mide desde el nodo vecino y con el movimiento que se haría para llegar a él.
	private MOVE bestMove(Game game, int from, MOVE lastMove, int target, boolean towards) {
		MOVE best = lastMove;
		int bestDist = towards ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		for (MOVE m : game.getPossibleMoves(from, lastMove)) {
			int next = game.getNeighbour(from, m);
			int d = game.getShortestPathDistance(next, target, m);
			if (towards ? d < bestDist : d > bestDist) {
				bestDist = d;
				best = m;
			}
		}
		return best;
	}
}