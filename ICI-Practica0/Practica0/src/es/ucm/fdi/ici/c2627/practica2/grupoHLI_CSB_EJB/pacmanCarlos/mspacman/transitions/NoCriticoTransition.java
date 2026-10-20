package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.transitions;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.fsm.Transition;

/** EscapeDeEmergencia -> Evadir: ¬crítico y hay salida segura. */
public class NoCriticoTransition implements Transition {

    @Override
    public boolean evaluate(Input in) {
        MsPacManInput input = (MsPacManInput) in;
        return !input.isCritico() && input.isSalidaSegura();
    }

    @Override
    public String toString() {
        return "no crítico y salida segura";
    }
}
