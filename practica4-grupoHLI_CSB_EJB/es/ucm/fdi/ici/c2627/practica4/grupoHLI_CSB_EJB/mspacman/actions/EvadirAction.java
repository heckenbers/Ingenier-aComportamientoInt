package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Parametros;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;

/**
 * evadir (Evadir de la V3): la ruta con más ventaja sobre los fantasmas que acabe en una
 * zona abierta, sin dejar de comer. Si pasa por la pill de cebo y ya vale la pena, la prefiere.
 */
public class EvadirAction extends AccionPacMan {

    public EvadirAction(Contexto contexto) {
        super("evadir", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        MapaNivel mapa = in.getMapa();
        boolean ceboRentable = in.getValorCebo() >= Parametros.VALOR_MIN_CEBO;
        return Navegacion.elegir(in, r -> {
            double bonusPoder = 0;
            if (r.getPosPoder() >= 0)
                bonusPoder = (r.getNodoPoder() == in.getPildoraCebo() && ceboRentable) ? 100 : -50;
            return 4.0 * Navegacion.seguridad(r)
                    + 2.0 * mapa.getApertura(r.getFin())
                    + 2.0 * r.getPildoras()
                    + bonusPoder
                    - (r.isColgante() ? Parametros.PENAL_COLGANTE : 0);
        });
    }
}
