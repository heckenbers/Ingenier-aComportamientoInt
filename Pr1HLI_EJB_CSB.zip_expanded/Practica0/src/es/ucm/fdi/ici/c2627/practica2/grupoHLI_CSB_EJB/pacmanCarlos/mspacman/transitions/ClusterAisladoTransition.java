package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.Parametros;
import es.ucm.fdi.ici.fsm.Transition;

/**
 * IrAPíldoraRentable -> LimpiarZonaAislada.
 * Hay un clúster aislado y el margen es holgado (ningún fantasma activo en
 * MARGEN_HOLGADO ticks), así que desviarse sale barato.
 */
public class ClusterAisladoTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput input = (MsPacManInput) in;
        return input.getPildorasAisladas().length > 0
                && input.getAmenazaEnPacman() > Parametros.MARGEN_HOLGADO;
    }

    @Override
    public String toString() {
        return "clúster aislado y margen holgado";
    }
}
