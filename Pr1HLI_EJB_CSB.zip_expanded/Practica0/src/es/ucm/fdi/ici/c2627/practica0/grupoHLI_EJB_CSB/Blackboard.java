package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/**
 * Memoria compartida de la HFSM: lo que hay que recordar entre ticks (histéresis, contadores, nivel) y los
 * hechos que se calculan una vez por tick o por cruce para que las guardas solo lean campos.
 */
public final class Blackboard {

    public enum Phase { OPENING, MIDGAME, ENDGAME }

    // ---- Cada tick
    public int tick;                       // Game.getTotalTime()
    public int node = -1;
    public MOVE last = MOVE.NEUTRAL;
    public int pills, powers, levelPillTotal;
    public Phase phase = Phase.OPENING;
    public int lives = Constants.NUM_LIVES;
    public int deaths;
    public boolean anyEdible;
    public boolean powerEatenNow;          // desapareció una píldora de poder o algún fantasma pasó de 0 a comestible
    public int minEdible = Integer.MAX_VALUE;
    public int lastFoodTick;
    public boolean resetRequested;         // nivel nuevo, partida nueva o muerte: la HFSM vuelve al estado inicial

    // ---- Memoria entre decisiones
    public boolean panic;
    public int panicDeadline;
    public int clearCount;                 // decisiones consecutivas de Evade con margen > δ_clear
    public int lastMargin;
    public int lastBaitOkTick = -100_000;
    public int prevDecisionNode = -1;
    public MOVE committed = MOVE.NEUTRAL; // último movimiento comprometido
    public MazeGraph graph;

    // ---- Hechos del cruce actual (solo válidos si atJunction)
    public boolean atJunction;
    public MOVE[] options = new MOVE[0];
    public final List<MazeGraph.Edge> exits = new ArrayList<MazeGraph.Edge>();
    public boolean nearNow, hasHuntTarget, pillsAdjacent, baitOk, panicCandidate;
    public final List<GHOST> huntTargets = new ArrayList<GHOST>();

    // ---- Cachés de un tick
    final Map<Long, Integer> threatCache = new HashMap<Long, Integer>();
    final Map<MazeGraph.Edge, Integer> marginCache = new HashMap<MazeGraph.Edge, Integer>();
    final Map<Integer, Integer> reachCache = new HashMap<Integer, Integer>();
    private int[] pillArr = new int[0], powerArr = new int[0];
    private Set<Integer> pillSet;

    private int mazeIndex = -1;
    private int prevTime = -1, prevLives = -1, prevTotal = -1, prevPowers = -1;
    private final int[] prevEdible = new int[GHOST.values().length];

    public void update(GameView v) {
        int maze = v.mazeIndex();
        int time = v.totalTime();
        int livesNow = v.lives();
        node = v.pacmanNode();
        last = v.pacmanLastMove();
        pillArr = v.activePills();
        powerArr = v.activePowerPills();
        pills = pillArr.length;
        powers = powerArr.length;
        int total = pills + powers;

        boolean first = prevTime < 0;
        boolean newGame = !first && time < prevTime;
        boolean newLevel = !first && (maze != mazeIndex || total > prevTotal);
        boolean died = !first && !newGame && livesNow < prevLives;

        if (graph == null || maze != mazeIndex) {
            graph = MazeGraph.build(v);
        }
        if (first || newGame || newLevel) {
            levelPillTotal = total;
            lastFoodTick = time;
            deaths = 0;
            Arrays.fill(prevEdible, 0);
            panic = false;
            prevDecisionNode = -1;
            resetRequested = true;
        }
        if (died) {
            deaths++;
            panic = false;
            prevDecisionNode = -1;
            resetRequested = true;
        }
        mazeIndex = maze;
        tick = time;
        lives = livesNow;
        if (!first && !newGame && !newLevel && total < prevTotal) lastFoodTick = time;

        boolean rose = false;
        anyEdible = false;
        minEdible = Integer.MAX_VALUE;
        for (GHOST g : GHOST.values()) {
            int e = v.ghostEdibleTime(g);
            if (e > 0) {
                anyEdible = true;
                minEdible = Math.min(minEdible, e);
                if (prevEdible[g.ordinal()] == 0) rose = true;
            }
            prevEdible[g.ordinal()] = e;
        }
        powerEatenNow = !first && !newGame && !newLevel && (powers < prevPowers || rose);
        if (anyEdible) panic = false;

        if (total <= Config.K_ENDGAME) phase = Phase.ENDGAME;
        else if (levelPillTotal > 0 && total > Config.OPENING_FRACTION * levelPillTotal) phase = Phase.OPENING;
        else phase = Phase.MIDGAME;

        prevTime = time;
        prevLives = livesNow;
        prevTotal = total;
        prevPowers = powers;

        threatCache.clear();
        marginCache.clear();
        reachCache.clear();
        pillSet = null;
    }

    public int[] pillNodes() {
        return pillArr;
    }

    public int[] powerNodes() {
        return powerArr;
    }

    private Set<Integer> pillSet() {
        if (pillSet == null) {
            pillSet = new HashSet<Integer>();
            for (int p : pillArr) pillSet.add(p);
        }
        return pillSet;
    }

    /** Píldoras normales en el pasillo (10 puntos cada una). */
    public int pillsOn(MazeGraph.Edge e) {
        Set<Integer> s = pillSet();
        int n = 0;
        for (int x : e.nodes) if (s.contains(x)) n++;
        return n;
    }

    /** Nodo de la píldora de poder que hay en el pasillo, o -1. */
    public int powerNodeOn(MazeGraph.Edge e) {
        for (int p : powerArr) {
            for (int x : e.nodes) if (x == p) return p;
        }
        return -1;
    }

    public boolean stalled() {
        return tick - lastFoodTick > Config.STALL_TICKS;
    }

    /** Quedan pocos ticks para MAX_TIME: cada vida sobrante suma 800, así que no se arriesga. */
    public boolean cautious() {
        return Constants.MAX_TIME - tick <= Config.CAUTIOUS_TICKS_LEFT;
    }
}
