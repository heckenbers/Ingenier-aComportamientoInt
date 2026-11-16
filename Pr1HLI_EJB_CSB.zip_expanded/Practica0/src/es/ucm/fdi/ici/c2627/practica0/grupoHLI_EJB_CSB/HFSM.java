package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Motor de la máquina jerárquica. Cada tick: actualiza el blackboard, evalúa transiciones y devuelve un
 * movimiento. Solo en un cruce se analiza la situación y se ejecuta el doAction de la hoja; fuera de un
 * cruce se devuelve la única salida legal sin tocar ninguna heurística.
 */
public final class HFSM {

    /** Tope de transiciones encadenadas en un mismo tick (evita rebotes infinitos). */
    private static final int MAX_ENCADENADAS = 3;

    private final State root;
    private final Blackboard bb;
    private final GameAdapter adapter = new GameAdapter();
    private State leaf;

    public HFSM(State root, Blackboard bb) {
        this.root = root;
        this.bb = bb;
        this.leaf = null;
    }

    public Blackboard blackboard() {
        return bb;
    }

    /** Nombre de la hoja activa (para tests y trazas). */
    public String currentLeaf() {
        return leaf == null ? "-" : leaf.name;
    }

    public MOVE update(Game game) {
        return update(adapter.set(game));
    }

    public MOVE update(GameView v) {
        bb.update(v);
        if (leaf == null || bb.resetRequested) {
            restart(v);
            bb.resetRequested = false;
        }

        MOVE[] options = v.possibleMoves(bb.node, bb.last);
        boolean junction = options.length > 1;
        bb.atJunction = junction;
        bb.options = options;
        if (junction) {
            Heuristics.analyse(v, bb, options);
        }

        for (int i = 0; i < MAX_ENCADENADAS; i++) {
            if (!fireOne(v, junction)) break;
        }

        MOVE m;
        if (!junction) {
            m = options.length == 1 ? options[0] : (options.length == 0 ? MOVE.NEUTRAL : bb.last);
        } else {
            m = leaf.act(v, bb);
            if (!contains(options, m)) m = options[0];     // la hoja nunca puede sacarnos de lo legal
            bb.prevDecisionNode = bb.node;
        }
        bb.committed = m;
        return m;
    }

    // ----------------------------------------------------------------- transiciones

    /** Evalúa el camino activo de la raíz a la hoja (padres primero) y dispara la primera guarda cierta. */
    private boolean fireOne(GameView v, boolean junction) {
        List<State> chain = new ArrayList<State>();
        for (State s = leaf; s != null; s = s.parent) chain.add(s);
        Collections.reverse(chain);
        for (State s : chain) {
            for (Transition t : s.transitions()) {
                if (t.junctionOnly && !junction) continue;
                if (t.guard.test(v, bb)) {
                    fire(v, s, t);
                    return true;
                }
            }
        }
        return false;
    }

    /** Sale de los estados que no contienen al destino (hasta el ancestro común), aplica el efecto y entra. */
    private void fire(GameView v, State source, Transition t) {
        List<State> ancestors = new ArrayList<State>();
        for (State a = t.target.parent; a != null; a = a.parent) ancestors.add(a);
        for (State s = leaf; s != null && !ancestors.contains(s); s = s.parent) {
            s.leave(v, bb);
        }
        if (t.effect != null) t.effect.execute(v, bb);
        descend(v, t.target);
    }

    private void descend(GameView v, State s) {
        s.enter(v, bb);
        while (s.initialChild != null) {
            s = s.initialChild;
            s.enter(v, bb);
        }
        leaf = s;
    }

    private void restart(GameView v) {
        descend(v, root);
    }

    private static boolean contains(MOVE[] a, MOVE m) {
        for (MOVE x : a) if (x == m) return true;
        return false;
    }
}
