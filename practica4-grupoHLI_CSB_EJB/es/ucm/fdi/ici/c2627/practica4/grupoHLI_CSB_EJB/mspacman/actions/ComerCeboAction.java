package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;

/** comerCebo: a por la píldora de cebo por la ruta más segura, para comérsela ya. */
public class ComerCeboAction extends AccionPacMan {

    public ComerCeboAction(Contexto contexto) {
        super("comerCebo", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        int p = in.getPildoraCebo();
        if (p < 0)
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        return Navegacion.irA(in, p, true);
    }
}
