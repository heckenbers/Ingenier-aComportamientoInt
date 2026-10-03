package es.ucm.fdi.ici.c2627.practica0.grupoIndividual;

import java.awt.Color;
import java.util.Random;

import pacman.controllers.PacmanController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;
import pacman.game.GameView;

public class MsPacManP1 extends PacmanController{
	
	private Random rnd = new Random();
	private MOVE[] allMoves = MOVE.values();
	private Color[] colours = { Color.RED, Color.PINK, Color.CYAN, Color.ORANGE };

	@Override
	public MOVE getMove(Game game, long timeDue) {
		
		
		int[] activePowerPills = game.getActivePowerPillsIndices();
		
		// -- VISUALIZAR PILDORAS DE PODER Y FANTASMAS
	    for (int i = 0; i < activePowerPills.length; i++)
	        GameView.addLines(game, Color.CYAN,
	                game.getPacmanCurrentNodeIndex(), activePowerPills[i]);

	    // Camino hacia cada fantasma
	    for (GHOST g : GHOST.values()) {
	        int ghost    = game.getGhostCurrentNodeIndex(g);
	        int mspacman = game.getPacmanCurrentNodeIndex();
	        // GHOST es un enum: ordinal() da su posición en la declaración
	        // (BLINKY 0, PINKY 1, INKY 2, SUE 3), y por eso vale de índice
	        if (game.getGhostLairTime(g) <= 0)
	            GameView.addPoints(game, colours[g.ordinal()],
	                    game.getShortestPath(ghost, mspacman));
	    }

	    return allMoves[rnd.nextInt(allMoves.length)];
	}

}
