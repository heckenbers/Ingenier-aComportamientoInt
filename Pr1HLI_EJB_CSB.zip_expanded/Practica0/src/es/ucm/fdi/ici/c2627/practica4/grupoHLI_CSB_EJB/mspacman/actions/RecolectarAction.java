package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;

/**
 * recolectar (IrAPildoraRentable de la V3): la ruta segura que más píldoras recoge,
 * priorizando grupos grandes y cercanos, y sin pisar las píldoras de poder.
 */
public class RecolectarAction extends AccionPacMan {

    public RecolectarAction(Contexto contexto) {
        super("recolectar", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
    }
}
