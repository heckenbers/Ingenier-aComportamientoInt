package es.ucm.fdi.ici.c2627.practica3.grupoYY.mspacman.actions;

import java.util.ArrayList;
import java.util.List;

import es.ucm.fdi.ici.rules.RulesAction;
import jess.Fact;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** Huye eligiendo el movimiento que deja a TODOS los fantasmas peligrosos lo más lejos posible. */
public class HuirSeguraAction implements RulesAction {

    @Override
    public MOVE execute(Game game) {
        int pacman = game.getPacmanCurrentNodeIndex();

        // 1. Quiénes son peligrosos (mismo criterio que HuirAction)
        List<GHOST> peligrosos = new ArrayList<>();
        for (GHOST g : GHOST.values()) {
            if (!game.isGhostEdible(g) && game.getGhostLairTime(g) <= 0) {
                peligrosos.add(g);
            }
        }
        if (peligrosos.isEmpty()) return MOVE.NEUTRAL;

        // 2. Puntuar cada movimiento posible
        MOVE mejorMov = MOVE.NEUTRAL;
        int mejorMin = -1;    // mayor "distancia mínima" encontrada
        int mejorSuma = -1;   // desempate: mayor suma de distancias

        for (MOVE m : game.getPossibleMoves(pacman)) {
            int vecino = game.getNeighbour(pacman, m);   // nodo al que llegaría
            if (vecino == -1) continue;

            int min = Integer.MAX_VALUE;
            int suma = 0;
            for (GHOST g : peligrosos) {
                int d = game.getShortestPathDistance(
                        game.getGhostCurrentNodeIndex(g), vecino, game.getGhostLastMoveMade(g));
                min = Math.min(min, d);
                suma += d;
            }

            // 3. Quedarse con el mejor
            if (min > mejorMin || (min == mejorMin && suma > mejorSuma)) {
                mejorMin = min;
                mejorSuma = suma;
                mejorMov = m;
            }
        }
        return mejorMov;
    }

    @Override
    public void parseFact(Fact actionFact) { }

    @Override
    public String getActionId() { return "huirSegura"; }
}