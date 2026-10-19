package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;

/**
 * Hay un fantasma no comestible, fuera de la cárcel, a menos de 'limite' nodos por los pasillos (cada
 * fantasma medido con su último movimiento). Se usa con 25 para entrar en HUIR y con 30 para seguir.
 */
public class FantasmaPeligrosoCercaTransition implements Transition {

    private final int limite;

    public FantasmaPeligrosoCercaTransition(int limite) {
        this.limite = limite;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput m = (MsPacManInput) in;
        return m.getNearestDangerous() != null && m.getDistNearestDangerous() < limite;
    }

    @Override
    public String toString() {
        return "Fantasma peligroso a menos de " + limite;
    }
}