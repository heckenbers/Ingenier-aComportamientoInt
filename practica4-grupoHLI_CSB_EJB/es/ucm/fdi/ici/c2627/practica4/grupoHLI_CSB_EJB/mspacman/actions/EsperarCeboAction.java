package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Parametros;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * esperarCebo (la patrulla de PrepararCebo de la V3): da vueltas a RADIO_PATRULLA de la
 * píldora de cebo sin pisarla, comiendo lo que pille, mientras los fantasmas se acercan.
 * Cuándo comérsela ya no lo decide la acción, sino el FCL (salida comerCebo).
 */
public class EsperarCeboAction extends AccionPacMan {

    public EsperarCeboAction(Contexto contexto) {
        super("esperarCebo", contexto);
    }

    @Override
    protected MOVE execute(Percepcion in) {
        Game game = in.getGame();
        int p = in.getPildoraCebo();
        if (p < 0)
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);
        contexto.getMemoria().registrarEspera(game.getTotalTime());
        return Navegacion.elegir(in, r -> {
            int d = game.getShortestPathDistance(r.getFin(), p);
            return -5.0 * Math.abs(d - Parametros.RADIO_PATRULLA)
                    + 2.0 * r.getPildoras()
                    + Math.min(r.getMargen(), 30)
                    - (r.getPosPoder() >= 0 ? Parametros.PENAL_PODER : 0)
                    - (r.isColgante() ? Parametros.PENAL_COLGANTE : 0);
        });
    }
}
