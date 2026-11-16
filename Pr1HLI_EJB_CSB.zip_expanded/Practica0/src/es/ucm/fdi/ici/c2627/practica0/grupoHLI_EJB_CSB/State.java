package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.ArrayList;
import java.util.List;

import pacman.game.Constants.MOVE;

/**
 * Estado de la HFSM. Si tiene hijos es compuesto y al entrar se baja a su hijo inicial; si no, es una hoja
 * y su doAction elige el movimiento. Las transiciones de un estado llevan a hermanos suyos.
 */
public final class State {
    public final String name;
    public State parent;
    public final List<State> children = new ArrayList<State>();
    public State initialChild;
    public Action entry, doAction, exit;
    private final List<Transition> transitions = new ArrayList<Transition>();

    public State(String name) {
        this.name = name;
    }

    public State child(State c, boolean initial) {
        c.parent = this;
        children.add(c);
        if (initial) initialChild = c;
        return this;
    }

    public State doing(Action a) {
        this.doAction = a;
        return this;
    }

    public State onEntry(Action a) {
        this.entry = a;
        return this;
    }

    public State onExit(Action a) {
        this.exit = a;
        return this;
    }

    public State add(Transition t) {
        transitions.add(t);
        transitions.sort((a, b) -> Integer.compare(a.priority, b.priority));
        return this;
    }

    public List<Transition> transitions() {
        return transitions;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    void enter(GameView in, Blackboard bb) {
        if (entry != null) entry.execute(in, bb);
        if (doAction != null) doAction.onEnter(in, bb);
    }

    void leave(GameView in, Blackboard bb) {
        if (doAction != null) doAction.onExit(in, bb);
        if (exit != null) exit.execute(in, bb);
    }

    MOVE act(GameView in, Blackboard bb) {
        return doAction == null ? MOVE.NEUTRAL : doAction.execute(in, bb);
    }
}
