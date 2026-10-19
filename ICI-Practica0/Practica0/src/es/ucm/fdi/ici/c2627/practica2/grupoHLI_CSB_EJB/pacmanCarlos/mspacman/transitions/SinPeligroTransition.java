package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;

/**
 * SUPERVIVENCIA -> RECOLECTAR (con historia).
 * ¬peligro durante al menos 'ticks' ticks seguidos (histéresis para no
 * oscilar entre los dos superestados).
 */
public class SinPeligroTransition implements Transition {

    private final int ticks;
    private int contador;
    private int ultimoTick = -10;

    public SinPeligroTransition(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput input = (MsPacManInput) in;
        int tick = input.getGame().getTotalTime();
        if (tick != ultimoTick + 1)             // se acaba de entrar en SUPERVIVENCIA
            contador = 0;
        ultimoTick = tick;
        contador = input.isPeligro() ? 0 : contador + 1;
        return contador >= ticks;
    }

    @Override
    public String toString() {
        return "sin peligro " + ticks + " ticks";
    }
}
