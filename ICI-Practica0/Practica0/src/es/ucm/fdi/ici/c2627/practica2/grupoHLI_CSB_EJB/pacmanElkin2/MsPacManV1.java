package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.pacmanElkin2.mspacman.MsPacManAction;

/**
 * v1 · Misma máquina que V0, con un único cambio: el movimiento seguro (el que usan las acciones cuando no hay
 * ruta libre, sobre todo al huir) mide la ventaja sobre los fantasmas en el primer cruce al que lleva cada
 * movimiento y no en el nodo de al lado. Ver diario.md.
 */
public class MsPacManV1 extends MsPacManV0 {

    public MsPacManV1() {
        super();
        for (MsPacManAction accion : getAcciones()) {
            accion.setSeguridadEnPasillo(true);
        }
    }

    @Override
    public String getName() {
        return "MsPacMan FSM grupoHLI_CSB_EJB V1";
    }
}