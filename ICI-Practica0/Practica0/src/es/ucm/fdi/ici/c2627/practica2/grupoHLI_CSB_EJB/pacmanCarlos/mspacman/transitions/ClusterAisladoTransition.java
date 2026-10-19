package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Parametros;

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
