package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;

/** LimpiarZonaAislada -> IrAPíldoraRentable: ya no queda ningún clúster aislado. */
public class ClusterRecogidoTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        return ((MsPacManInput) in).getPildorasAisladas().length == 0;
    }

    @Override
    public String toString() {
        return "clúster recogido";
    }
}
