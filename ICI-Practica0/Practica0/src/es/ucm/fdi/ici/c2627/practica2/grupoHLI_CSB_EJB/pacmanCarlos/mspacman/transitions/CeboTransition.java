package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MemoriaPacMan;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;

/**
 * IrAPíldoraRentable -> PrepararCebo.
 * poder_rentable y una píldora de poder segura, fuera de callejones, con
 * valor_cadena >= VALOR_MIN_CEBO; o quedan pocas píldoras y aún hay de poder.
 * No se vuelve a intentar durante ENFRIAMIENTO_CEBO ticks tras abandonar uno.
 */
public class CeboTransition implements Transition {

    private final MemoriaPacMan memoria;

    public CeboTransition(MemoriaPacMan memoria) {
        this.memoria = memoria;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput input = (MsPacManInput) in;
        if (memoria.ceboEnEnfriamiento(input.getGame().getTotalTime()))
            return false;
        return input.isCeboDisponible()
                || (input.isPocasPildoras() && input.getPildoraCebo() >= 0);
    }

    @Override
    public String toString() {
        return "cebo rentable o pocas píldoras";
    }
}
