package es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Huye sin dejar de comer: entre los movimientos que no acercan a ningún fantasma peligroso,
 * elige el que deja más cerca la píldora más próxima. Si no hay ninguno seguro, elige el que
 * deja a los fantasmas más lejos (el criterio anterior).
 */
public class HuirSeguraAction implements RulesAction {

    @Override
    public MOVE execute(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();

        // 1. Quiénes son peligrosos (mismo criterio que HuirAction y MsPacManInput)
        List<GHOST> peligrosos = new ArrayList<>();
        for (GHOST g : GHOST.values()) {
            if (!game.isGhostEdible(g) && game.getGhostLairTime(g) <= 0) peligrosos.add(g);
        }
        if (peligrosos.isEmpty()) return MOVE.NEUTRAL;

        // 2. Píldoras que quedan (normales y de poder)
        int[] normales = game.getActivePillsIndices();
        int[] poder = game.getActivePowerPillsIndices();

        MOVE mejorSeguro = null;          // seguro y más cerca de una píldora
        int mejorDistPildora = Integer.MAX_VALUE;
        MOVE respaldo = MOVE.NEUTRAL;     // el que más aleja de los fantasmas
        int mejorMin = -1;

        // 3. Solo movimientos legales: sin media vuelta (SUPUESTO: existe esta sobrecarga)
        for (MOVE m : game.getPossibleMoves(pacman, game.getPacmanLastMoveMade())) {
            int vecino = game.getNeighbour(pacman, m);
            if (vecino == -1) continue;

            boolean seguro = true;
            int min = Integer.MAX_VALUE;
            for (GHOST g : peligrosos) {
                int nodoG = game.getGhostCurrentNodeIndex(g);
                MOVE ult = game.getGhostLastMoveMade(g);
                int dAhora = game.getShortestPathDistance(nodoG, pacman, ult);
                int dDespues = game.getShortestPathDistance(nodoG, vecino, ult);
                if (dAhora < 0 || dDespues < 0) continue;   // sin dato: ese fantasma no decide
                if (dDespues < dAhora) seguro = false;      // este paso me acerca a él
                min = Math.min(min, dDespues);
            }

            // Respaldo: el movimiento que deja al fantasma más cercano más lejos
            if (min != Integer.MAX_VALUE && min > mejorMin) {
                mejorMin = min;
                respaldo = m;
            }

            // Objetivo: entre los seguros, el que deja más cerca una píldora
            if (seguro) {
                int dp = distanciaAPildora(game, vecino, normales, poder);
                if (dp < mejorDistPildora) {
                    mejorDistPildora = dp;
                    mejorSeguro = m;
                }
            }
        }
        return (mejorSeguro != null) ? mejorSeguro : respaldo;
    }

    /** Distancia por camino desde 'nodo' a la píldora más cercana; MAX_VALUE si no queda ninguna. */
    private int distanciaAPildora(Game game, int nodo, int[] normales, int[] poder) {
        int mejor = Integer.MAX_VALUE;
        for (int p : normales) mejor = Math.min(mejor, game.getShortestPathDistance(nodo, p));
        for (int p : poder) mejor = Math.min(mejor, game.getShortestPathDistance(nodo, p));
        return mejor;
    }

    @Override
    public void parseFact(Fact actionFact) { }

    @Override
    public String getActionId() { return "huirSegura"; }
}