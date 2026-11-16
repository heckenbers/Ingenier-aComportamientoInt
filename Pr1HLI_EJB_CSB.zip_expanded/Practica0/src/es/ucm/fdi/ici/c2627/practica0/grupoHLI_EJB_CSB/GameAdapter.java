package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import pacman.game.Constants.DM;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/** GameView sobre el Game real. Aquí se comprueban los null y los -1 que devuelve Game. */
public final class GameAdapter implements GameView {

    private static final int[] NONE = new int[0];
    private static final MOVE[] NO_MOVES = new MOVE[0];

    private Game g;

    public GameAdapter set(Game game) {
        this.g = game;
        return this;
    }

    private static MOVE move(MOVE m) {
        return m == null ? MOVE.NEUTRAL : m;
    }

    private static int[] arr(int[] a) {
        return a == null ? NONE : a;
    }

    private static int dist(int d) {
        return d < 0 ? UNREACHABLE : d;
    }

    @Override public int pacmanNode() { return g.getPacmanCurrentNodeIndex(); }
    @Override public MOVE pacmanLastMove() { return move(g.getPacmanLastMoveMade()); }

    @Override public int ghostNode(GHOST gh) { return g.getGhostCurrentNodeIndex(gh); }
    @Override public MOVE ghostLastMove(GHOST gh) { return move(g.getGhostLastMoveMade(gh)); }
    @Override public int ghostEdibleTime(GHOST gh) { return Math.max(0, g.getGhostEdibleTime(gh)); }
    @Override public int ghostLairTime(GHOST gh) { return Math.max(0, g.getGhostLairTime(gh)); }

    @Override
    public MOVE[] possibleMoves(int node, MOVE last) {
        if (node < 0) return NO_MOVES;
        MOVE[] m = g.getPossibleMoves(node, move(last));
        return m == null ? NO_MOVES : m;
    }

    @Override
    public int neighbour(int node, MOVE m) {
        if (node < 0 || m == null) return -1;
        return g.getNeighbour(node, m);
    }

    @Override
    public MOVE nextMoveTowards(int from, int to, MOVE last) {
        if (from < 0 || to < 0) return MOVE.NEUTRAL;
        return move(g.getNextMoveTowardsTarget(from, to, move(last), DM.PATH));
    }

    @Override
    public int shortestPathDistance(int a, int b) {
        if (a < 0 || b < 0) return UNREACHABLE;
        return dist(g.getShortestPathDistance(a, b));
    }

    @Override
    public int shortestPathDistance(int a, int b, MOVE last) {
        if (a < 0 || b < 0) return UNREACHABLE;
        if (last == null || last == MOVE.NEUTRAL) return shortestPathDistance(a, b);
        return dist(g.getShortestPathDistance(a, b, last));
    }

    @Override
    public int closestNode(int from, int[] targets) {
        if (from < 0 || targets == null || targets.length == 0) return -1;
        return g.getClosestNodeIndexFromNodeIndex(from, targets, DM.PATH);
    }

    @Override public int[] activePills() { return arr(g.getActivePillsIndices()); }
    @Override public int[] activePowerPills() { return arr(g.getActivePowerPillsIndices()); }
    @Override public int[] junctions() { return arr(g.getJunctionIndices()); }
    @Override public boolean isJunction(int node) { return node >= 0 && g.isJunction(node); }
    @Override public int mazeIndex() { return g.getMazeIndex(); }
    @Override public int numberOfNodes() { return g.getNumberOfNodes(); }
    @Override public int lairDoorNode() { return g.getGhostInitialNodeIndex(); }
    @Override public int totalTime() { return g.getTotalTime(); }
    @Override public int lives() { return g.getPacmanNumberOfLivesRemaining(); }
}
