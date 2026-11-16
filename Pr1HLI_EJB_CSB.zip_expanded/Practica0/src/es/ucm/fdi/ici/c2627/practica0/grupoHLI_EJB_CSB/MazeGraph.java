package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pacman.game.Constants.MOVE;

/** Grafo de cruces: nodos = cruces, aristas = pasillos. Se reconstruye cuando cambia el laberinto. */
public final class MazeGraph {

    private static final MOVE[] DIRS = {MOVE.UP, MOVE.RIGHT, MOVE.DOWN, MOVE.LEFT};

    /** Un pasillo: desde 'from' por 'move' hasta el siguiente cruce (incluido, es 'to'). */
    public static final class Edge {
        public final int from;
        public final MOVE move;
        public final int[] nodes;
        public final int length;
        public final int to;
        public final MOVE lastMove;       // movimiento del último paso (para saber dónde no se puede retroceder)
        public final boolean deadEnd;     // 'to' no es un cruce y no tiene salida

        Edge(int from, MOVE move, int[] nodes, MOVE lastMove, boolean deadEnd) {
            this.from = from;
            this.move = move;
            this.nodes = nodes;
            this.length = nodes.length;
            this.to = nodes[nodes.length - 1];
            this.lastMove = lastMove;
            this.deadEnd = deadEnd;
        }
    }

    private final Map<Long, Edge> edges = new HashMap<Long, Edge>();

    public static MazeGraph build(GameView v) {
        MazeGraph g = new MazeGraph();
        for (int j : v.junctions()) {
            for (MOVE m : DIRS) {
                Edge e = walk(v, j, m);
                if (e != null) g.edges.put(key(j, m), e);
            }
        }
        return g;
    }

    private static long key(int node, MOVE m) {
        return node * 8L + m.ordinal();
    }

    /** La arista que sale de 'node' por 'm': de la caché si es un cruce, recorriéndola si no (p. ej. tras reaparecer). */
    public Edge edge(GameView v, int node, MOVE m) {
        if (node < 0) return null;
        if (v.isJunction(node)) {
            Edge e = edges.get(key(node, m));
            if (e != null) return e;
        }
        return walk(v, node, m);
    }

    /** Recorre con getNeighbour hasta el siguiente cruce. null si 'm' no sale de 'from'. */
    public static Edge walk(GameView v, int from, MOVE m) {
        int cur = v.neighbour(from, m);
        if (cur < 0) return null;
        List<Integer> nodes = new ArrayList<Integer>();
        MOVE move = m;
        boolean deadEnd = false;
        for (int i = 0, max = v.numberOfNodes(); i < max; i++) {
            nodes.add(cur);
            if (v.isJunction(cur)) break;
            MOVE[] ops = v.possibleMoves(cur, move);
            if (ops.length == 0) {
                deadEnd = true;
                break;
            }
            if (ops.length > 1) break;                 // bifurcación que getJunctionIndices no marcó: se trata como cruce
            int next = v.neighbour(cur, ops[0]);
            if (next < 0) {
                deadEnd = true;
                break;
            }
            move = ops[0];
            cur = next;
        }
        int[] arr = new int[nodes.size()];
        for (int i = 0; i < arr.length; i++) arr[i] = nodes.get(i);
        return new Edge(from, m, arr, move, deadEnd);
    }
}
