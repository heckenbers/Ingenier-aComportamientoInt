package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import static pacman.game.Constants.EAT_DISTANCE;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Parametros;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;

/**
 * escapar (EscapeDeEmergencia de la V3): está acorralada o casi. Lo menos malo, por orden:
 * una píldora de poder a la que llega antes que nadie, un túnel con margen, y si no, la ruta
 * con más margen que acabe en el sitio más abierto.
 */
public class EscaparAction extends AccionPacMan {

    public EscaparAction(Contexto contexto) {
        super("escapar", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        MapaNivel mapa = in.getMapa();
        return Navegacion.elegirSinFiltro(in, r -> {
            double p = 0;
            if (r.getPosPoder() >= 0 && r.getMargenCorredor() > EAT_DISTANCE)
                p += 10000 - r.getPosPoder();
            if (r.isTunel() && r.getMargen() > EAT_DISTANCE)
                p += 300;
            p += 100.0 * Navegacion.seguridad(r);
            p += 5.0 * mapa.getApertura(r.getFin());
            if (r.isColgante())
                p -= Parametros.PENAL_COLGANTE;
            return p;
        });
    }
}
