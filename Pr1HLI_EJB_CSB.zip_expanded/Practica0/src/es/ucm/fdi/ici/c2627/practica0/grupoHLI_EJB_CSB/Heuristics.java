package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.List;

import pacman.game.Constants;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;

/** Heurísticas estáticas. Solo se llaman desde un cruce: en un pasillo no se toca nada de aquí. */
public final class Heuristics {
    private Heuristics() {
    }

    /** Margen a partir del cual una arista es segura: contacto a EAT_DISTANCE más la holgura δ. */
    public static final int SAFE = Constants.EAT_DISTANCE + Config.DELTA;

    // ------------------------------------------------------------ Fantasmas

    /** Distancia real del fantasma al nodo, respetando que no puede darse la vuelta. */
    static int ghostDistance(GameView v, GHOST g, int n) {
        int gn = v.ghostNode(g);
        if (gn < 0) return GameView.UNREACHABLE;
        return v.shortestPathDistance(gn, n, v.ghostLastMove(g));
    }

    /**
     * En cuántos turnos puede ser peligroso el fantasma g en el nodo n (UNREACHABLE si nunca).
     * Normal: su distancia. En la cárcel: lo que le falta para salir más la distancia desde la puerta.
     * Comestible (media velocidad): solo es peligroso cuando recupere el estado: max(2·d, tiempo comestible) (estimado).
     */
    static int threatTime(GameView v, Blackboard bb, GHOST g, int n) {
        long key = n * 4L + g.ordinal();
        Integer c = bb.threatCache.get(key);
        if (c != null) return c;
        int t;
        int lair = v.ghostLairTime(g);
        int edible = v.ghostEdibleTime(g);
        if (lair > 0) {
            int d = v.shortestPathDistance(v.lairDoorNode(), n);
            t = d >= GameView.UNREACHABLE ? GameView.UNREACHABLE : lair + d;
        } else {
            int d = ghostDistance(v, g, n);
            if (d >= GameView.UNREACHABLE) t = GameView.UNREACHABLE;
            else if (edible > 0) t = Math.max(2 * d, edible);
            else t = d;
        }
        bb.threatCache.put(key, t);
        return t;
    }

    // ------------------------------------------------------------ Seguridad

    /**
     * Choque frontal dentro del pasillo: un fantasma normal que ya está en él y viene hacia su origen. Su
     * distancia al cruce de destino no lo detecta (no puede darse la vuelta), así que se mira aparte.
     * Devuelve el margen (negativo = inseguro) o MARGIN_CAP si no hay choque.
     */
    static int headOn(GameView v, GHOST g, MazeGraph.Edge e) {
        if (v.ghostLairTime(g) > 0 || v.ghostEdibleTime(g) > 0) return Config.MARGIN_CAP;
        int gn = v.ghostNode(g);
        int idx = -1;
        for (int i = 0; i < e.nodes.length; i++) {
            if (e.nodes[i] == gn) {
                idx = i;
                break;
            }
        }
        if (idx < 0) return Config.MARGIN_CAP;
        if (ghostDistance(v, g, e.from) != idx + 1) return Config.MARGIN_CAP;   // se aleja o va a otro sitio
        return (idx + 1) / 2 - SAFE;                                            // nos cruzamos a mitad de camino
    }

    /**
     * Margen de supervivencia de recorrer 'e' empezando en el instante t: el peor (fantasma que llega antes
     * al cruce destino) menos mi llegada, y mirando 'depth' cruces más allá: tiene que existir una salida segura.
     */
    static int margin(GameView v, Blackboard bb, MazeGraph.Edge e, int t, int depth) {
        int arrive = t + e.length;
        int m0 = Config.MARGIN_CAP;
        for (GHOST g : GHOST.values()) {
            if (t == 0) m0 = Math.min(m0, headOn(v, g, e));      // solo con los fantasmas tal como están ahora
            int th = threatTime(v, bb, g, e.to);
            if (th >= GameView.UNREACHABLE) continue;
            m0 = Math.min(m0, th - arrive);
        }
        if (m0 <= SAFE || depth <= 0) return m0;
        if (e.deadEnd) return Math.min(m0, Config.DEAD_END_MARGIN);
        MOVE[] ops = v.possibleMoves(e.to, e.lastMove);
        if (ops.length == 0) return Math.min(m0, Config.DEAD_END_MARGIN);
        int best = Integer.MIN_VALUE;
        for (MOVE op : ops) {
            MazeGraph.Edge e2 = bb.graph.edge(v, e.to, op);
            if (e2 == null) continue;
            best = Math.max(best, margin(v, bb, e2, arrive, depth - 1));
        }
        if (best == Integer.MIN_VALUE) return Math.min(m0, Config.DEAD_END_MARGIN);
        return Math.min(m0, best);
    }

    static int margin(GameView v, Blackboard bb, MazeGraph.Edge e) {
        Integer c = bb.marginCache.get(e);
        if (c != null) return c;
        int m = margin(v, bb, e, 0, Config.DEPTH);
        bb.marginCache.put(e, m);
        return m;
    }

    public static boolean isSafe(GameView v, Blackboard bb, MazeGraph.Edge e) {
        return margin(v, bb, e) > SAFE;
    }

    /** 0 (margen amplio) a 1 (contacto inminente). */
    static double risk(int margin) {
        double r = (Config.RISK_HORIZON - margin) / (double) Config.RISK_HORIZON;
        return Math.max(0, Math.min(1, r));
    }

    // ------------------------------------------------------- Píldora de poder

    /** Fantasmas normales fuera de la cárcel a los que se llegaría desde la píldora antes de que acabe el miedo. */
    static int reachableGhosts(GameView v, Blackboard bb, int powerNode) {
        Integer c = bb.reachCache.get(powerNode);
        if (c != null) return c;
        int k = 0;
        for (GHOST g : GHOST.values()) {
            if (v.ghostLairTime(g) > 0 || v.ghostEdibleTime(g) > 0 || v.ghostNode(g) < 0) continue;
            int d = v.shortestPathDistance(powerNode, v.ghostNode(g));
            if (d <= Config.D_CADENA && 2 * d + 6 < Config.FEAR_ESTIMATE) k++;
        }
        bb.reachCache.put(powerNode, k);
        return k;
    }

    /** 200·(2^k − 1): lo que vale comer k fantasmas seguidos con una píldora. */
    static int chainGain(int k) {
        return Constants.GHOST_EAT_SCORE * ((1 << Math.min(k, 4)) - 1);
    }

    /**
     * ¿Conviene comer la píldora de poder 'powerNode' ahora? Sí si es de pánico, si la cadena esperada llega a
     * E_min o, en Endgame con píldoras normales restantes. Si es el último elemento del nivel, solo con cadena
     * (o si el nivel lleva demasiado sin avanzar: así siempre puede acabar).
     */
    static boolean shouldEatPowerPill(GameView v, Blackboard bb, int powerNode) {
        if (bb.panic) return true;
        int gain = chainGain(reachableGhosts(v, bb, powerNode));
        boolean lastItem = bb.pills == 0 && bb.powers <= 1;
        if (lastItem) return gain >= Config.E_MIN || bb.stalled();
        if (gain >= Config.E_MIN) return true;
        if (bb.pills == 0 && bb.stalled()) return true;
        return bb.phase == Blackboard.Phase.ENDGAME && bb.pills > 0;
    }

    // ---------------------------------------------------------------- Valor

    private static int futureValue(GameView v, Blackboard bb, MazeGraph.Edge e) {
        if (e.deadEnd) return 0;
        int best = 0;
        for (MOVE op : v.possibleMoves(e.to, e.lastMove)) {
            MazeGraph.Edge e2 = bb.graph.edge(v, e.to, op);
            if (e2 != null) best = Math.max(best, Math.min(10, bb.pillsOn(e2)));
        }
        return best;
    }

    /** Valor de la arista: puntos, ganancia de la cadena, menos riesgo y retroceso, más valor futuro. */
    static double edgeValue(GameView v, Blackboard bb, MazeGraph.Edge e, int margin) {
        double val = Constants.PILL * bb.pillsOn(e);
        int pn = bb.powerNodeOn(e);
        if (pn >= 0) {
            if (shouldEatPowerPill(v, bb, pn)) val += Constants.POWER_PILL + chainGain(reachableGhosts(v, bb, pn));
            else val -= Config.W_WASTE;
        }
        double mult = bb.cautious() ? Config.CAUTIOUS_RISK_MULT : 1.0;
        val -= Config.W_R * mult * risk(margin);
        if (e.to == bb.prevDecisionNode) val -= Config.W_B;
        val += Config.W_F * futureValue(v, bb, e);
        return val;
    }

    // ------------------------------------------------------------- Análisis

    /** Hechos del cruce: se calculan una vez y las guardas solo los leen. */
    static void analyse(GameView v, Blackboard bb, MOVE[] options) {
        bb.exits.clear();
        for (MOVE m : options) {
            MazeGraph.Edge e = bb.graph.edge(v, bb.node, m);
            if (e != null) bb.exits.add(e);
        }

        bb.nearNow = false;
        int normalsNear = 0;
        for (GHOST g : GHOST.values()) {
            if (v.ghostEdibleTime(g) > 0 || v.ghostNode(g) < 0) continue;
            for (MazeGraph.Edge e : bb.exits) {
                if (threatTime(v, bb, g, e.to) <= Config.THREAT_DIST || headOn(v, g, e) <= SAFE) {
                    bb.nearNow = true;
                    break;
                }
            }
            if (v.ghostLairTime(g) == 0 && v.shortestPathDistance(bb.node, v.ghostNode(g)) <= Config.D_CADENA) normalsNear++;
        }

        bb.huntTargets.clear();
        for (GHOST g : GHOST.values()) {
            int edible = v.ghostEdibleTime(g);
            if (edible <= 0 || v.ghostNode(g) < 0) continue;
            int d = v.shortestPathDistance(bb.node, v.ghostNode(g), bb.last);
            if (2 * d < edible) bb.huntTargets.add(g);
        }
        bb.hasHuntTarget = !bb.huntTargets.isEmpty();

        bb.pillsAdjacent = false;
        for (MazeGraph.Edge e : bb.exits) {
            if (bb.pillsOn(e) > 0) bb.pillsAdjacent = true;
        }

        bb.baitOk = bb.powers > 0 && !bb.panic && normalsNear >= 2;
        if (bb.baitOk) bb.lastBaitOkTick = bb.tick;

        bb.panicCandidate = false;
        if (bb.nearNow && bb.powers > 0) {
            int plainSafe = 0;
            boolean powerEdge = false;
            for (MazeGraph.Edge e : bb.exits) {
                if (bb.powerNodeOn(e) >= 0) powerEdge = true;
                else if (isSafe(v, bb, e)) plainSafe++;
            }
            bb.panicCandidate = powerEdge && plainSafe == 0;
        }
    }

    // ------------------------------------------------------------- Acciones

    private static final class Cand {
        final MazeGraph.Edge e;
        final int margin;
        final boolean safe;
        final double value;

        Cand(MazeGraph.Edge e, int margin, double value) {
            this.e = e;
            this.margin = margin;
            this.safe = margin > SAFE;
            this.value = value;
        }

        /** Las seguras ganan a las no seguras; entre seguras, el valor; entre no seguras, el margen. */
        boolean beats(Cand o) {
            if (o == null) return true;
            if (safe != o.safe) return safe;
            if (safe) return value > o.value;
            if (margin != o.margin) return margin > o.margin;
            return value > o.value;
        }
    }

    private static MOVE fallback(Blackboard bb) {
        return bb.options.length > 0 ? bb.options[0] : MOVE.NEUTRAL;
    }

    /** Harvest / SafeHarvest / Bait: la arista segura de mayor valor; si no hay ninguna, la de más margen. */
    static MOVE harvest(GameView v, Blackboard bb, boolean bait) {
        int power = bait ? v.closestNode(bb.node, bb.powerNodes()) : -1;
        Cand best = null;
        for (MazeGraph.Edge e : bb.exits) {
            int m = margin(v, bb, e);
            double val = edgeValue(v, bb, e, m);
            for (MazeGraph.Edge o : bb.exits) {          // no dejar píldoras sueltas atrás
                if (o == e) continue;
                int p = bb.pillsOn(o);
                if (p >= 1 && p <= Config.ISO_MAX) val -= Config.W_ISO * p;
            }
            if (power >= 0) {
                int d = v.shortestPathDistance(e.to, power);
                if (d < GameView.UNREACHABLE) val -= Config.W_BAIT * d;
            }
            Cand c = new Cand(e, m, val);
            if (c.beats(best)) best = c;
        }
        if (best == null) return fallback(bb);
        bb.lastMargin = best.margin;
        return best.e.move;
    }

    /** Evade / Retreat: el pasillo de mayor margen de supervivencia; desempata por píldoras. */
    static MOVE evade(GameView v, Blackboard bb, boolean countClear) {
        MazeGraph.Edge best = null;
        int bestMargin = Integer.MIN_VALUE;
        int bestPills = -1;
        for (MazeGraph.Edge e : bb.exits) {
            int m = margin(v, bb, e);
            int p = bb.pillsOn(e);
            if (m > bestMargin || (m == bestMargin && p > bestPills)) {
                best = e;
                bestMargin = m;
                bestPills = p;
            }
        }
        if (best == null) return fallback(bb);
        bb.lastMargin = bestMargin;
        if (countClear) bb.clearCount = bestMargin > Config.DELTA_CLEAR ? bb.clearCount + 1 : 0;
        return best.move;
    }

    /** Hacia 'target' pasando solo por pasillos seguros; si ninguno lo es, el de más margen. */
    static MOVE towards(GameView v, Blackboard bb, int target, boolean avoidPower) {
        MazeGraph.Edge bestSafe = null;
        int bestScore = Integer.MAX_VALUE;
        MazeGraph.Edge bestRisky = null;
        int bestMargin = Integer.MIN_VALUE;
        for (MazeGraph.Edge e : bb.exits) {
            int m = margin(v, bb, e);
            int score = contains(e, target) ? 0 : e.length + v.shortestPathDistance(e.to, target);
            if (avoidPower && bb.powerNodeOn(e) >= 0 && !shouldEatPowerPill(v, bb, bb.powerNodeOn(e))) {
                score += (int) Config.W_WASTE;
            }
            if (m > SAFE) {
                if (score < bestScore) {
                    bestScore = score;
                    bestSafe = e;
                }
            } else if (m > bestMargin) {
                bestMargin = m;
                bestRisky = e;
            }
        }
        MazeGraph.Edge pick = bestSafe != null ? bestSafe : bestRisky;
        if (pick == null) return fallback(bb);
        bb.lastMargin = margin(v, bb, pick);
        return pick.move;
    }

    /** Reposition: sin píldoras al lado, al grupo más cercano. */
    static MOVE reposition(GameView v, Blackboard bb) {
        int target = v.closestNode(bb.node, bb.pillNodes());
        if (target < 0) return harvest(v, bb, false);
        return towards(v, bb, target, true);
    }

    /** Hunt: al comestible de mejor cadena (más compañeros cerca, menos distancia); pánico: a la píldora de poder. */
    static MOVE hunt(GameView v, Blackboard bb) {
        List<GHOST> targets = bb.huntTargets;
        if (targets.isEmpty()) {
            if (bb.panic) return panicMove(v, bb);
            return harvest(v, bb, false);
        }
        GHOST bestG = null;
        int bestScore = Integer.MIN_VALUE;
        for (GHOST g : targets) {
            int gn = v.ghostNode(g);
            int d = v.shortestPathDistance(bb.node, gn, bb.last);
            int mates = 0;
            for (GHOST o : targets) {
                if (o != g && v.shortestPathDistance(gn, v.ghostNode(o)) <= Config.D_CADENA) mates++;
            }
            int score = 100 * mates - d;
            if (score > bestScore) {
                bestScore = score;
                bestG = g;
            }
        }
        MOVE m = v.nextMoveTowards(bb.node, v.ghostNode(bestG), bb.last);
        if (contains(bb.options, m)) {
            MazeGraph.Edge e = bb.graph.edge(v, bb.node, m);
            if (e != null && isSafe(v, bb, e)) {           // no comer y morir en el mismo turno
                bb.lastMargin = margin(v, bb, e);
                return m;
            }
        }
        return harvest(v, bb, false);
    }

    private static MOVE panicMove(GameView v, Blackboard bb) {
        MazeGraph.Edge best = null;
        int bestMargin = Integer.MIN_VALUE;
        for (MazeGraph.Edge e : bb.exits) {
            if (bb.powerNodeOn(e) < 0) continue;
            int m = margin(v, bb, e);
            if (m > bestMargin) {
                bestMargin = m;
                best = e;
            }
        }
        if (best != null) return best.move;
        int target = v.closestNode(bb.node, bb.powerNodes());
        if (target < 0) return evade(v, bb, false);
        return towards(v, bb, target, false);
    }

    private static boolean contains(MazeGraph.Edge e, int node) {
        for (int x : e.nodes) if (x == node) return true;
        return false;
    }

    private static boolean contains(MOVE[] a, MOVE m) {
        for (MOVE x : a) if (x == m) return true;
        return false;
    }
}
