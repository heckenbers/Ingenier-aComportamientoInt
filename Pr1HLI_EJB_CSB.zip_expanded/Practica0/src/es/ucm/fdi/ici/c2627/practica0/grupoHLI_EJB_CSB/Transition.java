package es.ucm.fdi.ici.c2627.practica0.grupoHLI_EJB_CSB;

import java.util.function.BiPredicate;

/**
 * Arco entre estados hermanos. 'priority' menor = se evalúa antes. Si junctionOnly es true solo se
 * evalúa en cruces (su guarda lee hechos que solo se calculan allí, y así en un pasillo no cuesta nada).
 */
public final class Transition {
    public final String name;
    public final State target;
    public final BiPredicate<GameView, Blackboard> guard;
    public final int priority;
    public final Action effect;          // opcional
    public final boolean junctionOnly;

    public Transition(String name, State target, BiPredicate<GameView, Blackboard> guard, int priority,
                      boolean junctionOnly, Action effect) {
        this.name = name;
        this.target = target;
        this.guard = guard;
        this.priority = priority;
        this.junctionOnly = junctionOnly;
        this.effect = effect;
    }
}
