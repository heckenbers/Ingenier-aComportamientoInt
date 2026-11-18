package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;

/**
 * irAPildora (nueva en la v6): derecha a la píldora normal más cercana, por una ruta segura
 * y sin gastar píldoras de poder. recolectar puntúa cada píldora por 1/distancia, así que
 * cuando las que quedan están lejos (o en un callejón) pesa más el margen y Ms. Pac-Man
 * deambulaba por zonas seguras sin comer: en la traza, 2000 turnos con las mismas píldoras.
 */
public class IrAPildoraAction extends AccionPacMan {

    public IrAPildoraAction(Contexto contexto) {
        super("irAPildora", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        int p = in.getPildoraCercana();
        if (p < 0)
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        return Navegacion.irA(in, p, false);
    }
}
