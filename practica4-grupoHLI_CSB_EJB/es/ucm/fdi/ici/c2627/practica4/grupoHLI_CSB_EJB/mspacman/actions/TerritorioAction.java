package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions;

import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Navegacion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Territorio;
import pacman.game.Constants.MOVE;

/**
 * territorio (nueva en la v14): el movimiento que deja más laberinto "suyo" (nodos a los que llega
 * antes que cualquier fantasma, ver Territorio). Una power pill dentro de la zona cuenta como
 * PESO_PODER nodos: es una salida.
 *
 * Por qué: las muertes contra los fantasmas buenos (Avanzado, Héctor, P3) llegan con la trampa
 * cerrada desde 30 turnos antes, con los fantasmas a 25-30 ticks. Las rutas de la V3 miran sólo
 * tres pasillos por delante; esto mira todo el laberinto que le queda libre.
 */
public class TerritorioAction extends AccionPacMan {

    static final int PESO_PODER = 60;

    private final double pesoPoder, pesoPildora;

    /** La de la v14: id territorio, power pill = 60 nodos, las píldoras no cuentan. */
    public TerritorioAction(Contexto contexto) {
        this("territorio", contexto, PESO_PODER, 0);
    }

    /**
     * Variantes con otro id (otra salida del FCL), para no cambiar la acción que usa la v14:
     * territorioComida (v18) y territorioPoder (v19).
     */
    public TerritorioAction(String id, Contexto contexto, double pesoPoder, double pesoPildora) {
        super(id, contexto);
        this.pesoPoder = pesoPoder;
        this.pesoPildora = pesoPildora;
    }

    @Override
    protected MOVE execute(Percepcion in) {
        MOVE mejor = null;
        double mejorValor = Double.NEGATIVE_INFINITY;
        for (MOVE m : Territorio.movimientos(in)) {
            double v = Territorio.tamano(in, m, pesoPoder, pesoPildora);
            if (v > mejorValor) { mejorValor = v; mejor = m; }
        }
        if (mejor == null || mejorValor <= 0)       // ningún movimiento deja zona segura: lo de escapar
            return Navegacion.elegirSinFiltro(in, r -> 100.0 * Navegacion.seguridad(r) + 5.0 * in.getMapa().getApertura(r.getFin()));
        return mejor;
    }
}
