package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MemoriaPacMan;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Parametros;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * RECOLECTAR / PrepararCebo.
 * Patrulla a RADIO_PATRULLA de una píldora de poder sin comérsela mientras los
 * fantasmas se acercan. Se la come cuando valor_cadena alcanza UMBRAL_CADENA,
 * o tras PACIENCIA_CEBO ticks si ya vale VALOR_MIN_CEBO (o quedan pocas píldoras).
 * Al comerla salta la transición RECOLECTAR -> PODER.
 */
public class PrepararCeboAction extends PacManAction {

    private final MemoriaPacMan memoria;

    public PrepararCeboAction(MemoriaPacMan memoria) {
        super("PrepararCebo");
        this.memoria = memoria;
    }

    @Override
    public MOVE execute(MsPacManInput in) {
        Game game = in.getGame();
        int p = in.getPildoraCebo();
        if (p < 0)                              // sin cebo: la transición saldrá en el siguiente tick
            return Navegacion.recolectar(in, in.getPildoras(), in.getTamClusters(), 0.5);

        int tick = game.getTotalTime();
        memoria.registrarCebo(tick, in.getValorCebo());
        int espera = memoria.getTicksEsperando(tick);

        boolean listo = in.getValorCebo() >= Parametros.UMBRAL_CADENA
                || (espera >= Parametros.PACIENCIA_CEBO
                        && (in.getValorCebo() >= Parametros.VALOR_MIN_CEBO || in.isPocasPildoras()));
        if (listo)
            return Navegacion.irA(in, p, true);

        // patrulla: quedarse a RADIO_PATRULLA de p, comiendo lo que pille por el camino
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
