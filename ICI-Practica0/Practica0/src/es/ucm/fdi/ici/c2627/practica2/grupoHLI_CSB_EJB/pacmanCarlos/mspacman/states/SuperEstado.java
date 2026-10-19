package es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.states;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import es.ucm.fdi.ici.Input;
import es.ucm.fdi.ici.fsm.FSM;
import es.ucm.fdi.ici.fsm.State;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MemoriaPacMan;
import pacman.game.Constants.MOVE;

/**
 * Superestado: un estado que contiene su propia FSM.
 *
 * Al entrar se reinicia la FSM interna (vuelve a su subestado inicial), salvo
 * si se viene de uno de los superestados de historiaDesde: entonces se
 * reanuda el subestado en el que estaba (historia superficial).
 *
 * La entrada se detecta por tick (si no se ejecutó en el tick anterior, se
 * acaba de entrar), así no depende de que la FSM llame a stop() al salir.
 */
public class SuperEstado implements State {

    private final String id;
    private final FSM fsm;
    private final MemoriaPacMan memoria;
    private final Set<String> historiaDesde;
    private int ultimoTick = -10;

    public SuperEstado(String id, FSM fsm, MemoriaPacMan memoria, String... historiaDesde) {
        this.id = id;
        this.fsm = fsm;
        this.memoria = memoria;
        this.historiaDesde = new HashSet<>(Arrays.asList(historiaDesde));
    }

    @Override
    public MOVE execute(Input in) {
        int tick = in.getGame().getTotalTime();
        boolean entrando = tick > ultimoTick + 1 || tick < ultimoTick;
        if (entrando) {
            String previo = memoria.getSuperestadoActivo();
            if (previo == null || !historiaDesde.contains(previo))
                fsm.reset();                    // sin historia: subestado inicial
        }
        ultimoTick = tick;
        memoria.setSuperestadoActivo(id);
        return fsm.run(in);
    }

    @Override
    public void stop() {
        ultimoTick = -10;                       // la próxima ejecución cuenta como entrada
    }

    /** Vuelve al subestado inicial (lo usa NuevoNivel desde el controlador). */
    public void reiniciar() {
        fsm.reset();
        ultimoTick = -10;
    }

    @Override
    public String toString() {
        return id;
    }
}
