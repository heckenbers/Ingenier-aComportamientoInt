package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MemoriaPacMan;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Parametros;

/**
 * PrepararCebo -> IrAPíldoraRentable.
 * Ya no hay cebo posible, o valor_cadena lleva PACIENCIA_CEBO/2 ticks por
 * debajo de VALOR_MIN_CEBO (los fantasmas no se agrupan) y quedan píldoras.
 * Al dispararse anota el abandono para el enfriamiento de CeboTransition.
 */
public class CeboAgotadoTransition implements Transition {

    private final MemoriaPacMan memoria;

    public CeboAgotadoTransition(MemoriaPacMan memoria) {
        this.memoria = memoria;
    }

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput input = (MsPacManInput) in;
        int tick = input.getGame().getTotalTime();
        boolean agotado = input.getPildoraCebo() < 0
                || (!input.isPocasPildoras()
                        && memoria.getTicksSinValor(tick) >= Parametros.PACIENCIA_CEBO / 2);
        if (agotado)
            memoria.abandonarCebo(tick);
        return agotado;
    }

    @Override
    public String toString() {
        return "cebo sin mejora";
    }
}
