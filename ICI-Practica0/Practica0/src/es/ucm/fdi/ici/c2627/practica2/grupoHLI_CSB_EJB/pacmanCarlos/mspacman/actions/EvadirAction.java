package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Parametros;
import pacman.game.Constants.MOVE;

/**
 * SUPERVIVENCIA / Evadir.
 * Toma la ruta con más ventaja sobre los fantasmas (peor caso, con lookahead
 * de varios pasillos) que acabe en una zona abierta, sin dejar de comer.
 * Si la ruta pasa por la píldora de cebo y ya merece la pena, la prefiere.
 */
public class EvadirAction extends PacManAction {

    public EvadirAction() {
        super("Evadir");
    }

    @Override
    public MOVE execute(MsPacManInput in) {
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
