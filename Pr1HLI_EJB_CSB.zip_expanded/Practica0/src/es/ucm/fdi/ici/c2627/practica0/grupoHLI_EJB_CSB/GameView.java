package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Adaptador fino sobre Game: solo lo que la HFSM necesita. Contrato de los resultados
 * (lo garantiza GameAdapter, donde se reciben los valores de Game):
 *  - nunca devuelve null: los MOVE ausentes son NEUTRAL y los arrays ausentes son vacíos;
 *  - las distancias inalcanzables o con índices -1 valen UNREACHABLE (no negativo);
 *  - neighbour() y closestNode() devuelven -1 si no existe el nodo: quien llama debe comprobarlo.
 */
public interface GameView {

    /** Distancia que se devuelve cuando no hay camino. Suficientemente grande y sin riesgo de desbordar al sumar. */
    int UNREACHABLE = 100_000;

    int pacmanNode();
    MOVE pacmanLastMove();

    int ghostNode(GHOST g);
    MOVE ghostLastMove(GHOST g);
    /** > 0: comestible. */
    int ghostEdibleTime(GHOST g);
    /** > 0: en la cárcel. */
    int ghostLairTime(GHOST g);

    MOVE[] possibleMoves(int node, MOVE last);
    int neighbour(int node, MOVE move);
    MOVE nextMoveTowards(int from, int to, MOVE last);

    int shortestPathDistance(int a, int b);
    int shortestPathDistance(int a, int b, MOVE last);
    int closestNode(int from, int[] targets);

    int[] activePills();
    int[] activePowerPills();

    int[] junctions();
    boolean isJunction(int node);
    int mazeIndex();
    int numberOfNodes();
    /** Nodo por el que los fantasmas salen de la cárcel. */
    int lairDoorNode();

    /** Tick de la partida (Game.getTotalTime). Desviación del enunciado: ver supuestos. */
    int totalTime();
    /** Vidas restantes (Game.getPacmanNumberOfLivesRemaining). */
    int lives();
}
