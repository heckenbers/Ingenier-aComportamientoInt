package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.fsm.Transition;

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
