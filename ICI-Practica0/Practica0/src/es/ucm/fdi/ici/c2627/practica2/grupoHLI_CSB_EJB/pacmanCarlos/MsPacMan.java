package es.ucm.fdi.ici.c2627.practica2.grupoYY;

import java.awt.Dimension;

import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MemoriaPacMan;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.Parametros;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.CazarFantasmaAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.EscapeDeEmergenciaAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.EvadirAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.IrAPildoraRentableAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.LimpiarZonaAisladaAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.PrepararCeboAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.actions.RecolectarEnPoderAction;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.states.EstadoConInput;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.states.SuperEstado;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.CeboAgotadoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.CeboTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.ClusterAisladoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.ClusterRecogidoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.CriticoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.NoCriticoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.PeligroTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.PlanCazaDisponibleTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.PlanCazaVacioTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.PoderActivoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.PorRevertirTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.SinPeligroTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoYY.mspacman.transitions.SinPoderTransition;
import es.ucm.fdi.ici.fsm.FSM;
import es.ucm.fdi.ici.fsm.observers.GraphFSMObserver;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Controlador de Ms Pac-Man con una HFSM:
 *
 * PACMAN (raíz)
 * ├── RECOLECTAR    (historia al volver de SUPERVIVENCIA)   [inicial]
 * │   ├── IrAPildoraRentable  [inicial]
 * │   ├── LimpiarZonaAislada
 * │   └── PrepararCebo
 * ├── SUPERVIVENCIA
 * │   ├── Evadir              [inicial]
 * │   └── EscapeDeEmergencia
 * └── PODER
 *     ├── CazarFantasma       [inicial]
 *     └── RecolectarEnPoder
 *
 * La transición de raíz NuevoNivel -> RECOLECTAR (válida desde cualquier
 * estado) se hace aquí en getMove: reinicia todas las máquinas y recalcula el
 * preproceso del laberinto. También se dispara cuando Pac-Man muere, porque
 * el juego recoloca a todos.
 */
public class MsPacMan extends PacmanController {

    private static final boolean DEBUG = false;     // a false antes de entregar

    private final MemoriaPacMan memoria = new MemoriaPacMan();
    private final FSM fsm;
    private final SuperEstado recolectar;
    private final SuperEstado supervivencia;
    private final SuperEstado poder;
    private int nivelAnterior = -1;

    public MsPacMan() {
        setName("MsPacMan YY");

        // ---------------- RECOLECTAR ----------------
        FSM fsmRecolectar = new FSM("RECOLECTAR");
        GraphFSMObserver gRecolectar = observar(fsmRecolectar, "RECOLECTAR");

        EstadoConInput irAPildora = new EstadoConInput(new IrAPildoraRentableAction());
        EstadoConInput limpiar    = new EstadoConInput(new LimpiarZonaAisladaAction());
        EstadoConInput cebo       = new EstadoConInput(new PrepararCeboAction(memoria));

        // orden de add() = prioridad: oportunidad (cebo) antes que limpieza
        fsmRecolectar.add(irAPildora, new CeboTransition(memoria), cebo);
        fsmRecolectar.add(irAPildora, new ClusterAisladoTransition(), limpiar);
        fsmRecolectar.add(limpiar,    new ClusterRecogidoTransition(), irAPildora);
        fsmRecolectar.add(cebo,       new CeboAgotadoTransition(memoria), irAPildora);
        fsmRecolectar.ready(irAPildora);

        // ---------------- SUPERVIVENCIA ----------------
        FSM fsmSupervivencia = new FSM("SUPERVIVENCIA");
        GraphFSMObserver gSupervivencia = observar(fsmSupervivencia, "SUPERVIVENCIA");

        EstadoConInput evadir = new EstadoConInput(new EvadirAction());
        EstadoConInput escape = new EstadoConInput(new EscapeDeEmergenciaAction());

        fsmSupervivencia.add(evadir, new CriticoTransition(), escape);
        fsmSupervivencia.add(escape, new NoCriticoTransition(), evadir);
        fsmSupervivencia.ready(evadir);

        // ---------------- PODER ----------------
        FSM fsmPoder = new FSM("PODER");
        GraphFSMObserver gPoder = observar(fsmPoder, "PODER");

        EstadoConInput cazar        = new EstadoConInput(new CazarFantasmaAction());
        EstadoConInput recolectarEP = new EstadoConInput(new RecolectarEnPoderAction());

        fsmPoder.add(cazar,        new PlanCazaVacioTransition(), recolectarEP);
        fsmPoder.add(recolectarEP, new PlanCazaDisponibleTransition(), cazar);
        fsmPoder.ready(cazar);

        // ---------------- raíz ----------------
        recolectar    = new SuperEstado("RECOLECTAR", fsmRecolectar, memoria, "SUPERVIVENCIA");
        supervivencia = new SuperEstado("SUPERVIVENCIA", fsmSupervivencia, memoria);
        poder         = new SuperEstado("PODER", fsmPoder, memoria);

        fsm = new FSM("MsPacMan");
        GraphFSMObserver gRaiz = observar(fsm, "MsPacMan");

        // seguridad primero, después oportunidad, después vuelta a la normalidad
        fsm.add(recolectar,    new PeligroTransition(), supervivencia);
        fsm.add(recolectar,    new PoderActivoTransition(), poder);
        fsm.add(supervivencia, new PoderActivoTransition(), poder);
        fsm.add(supervivencia, new SinPeligroTransition(Parametros.HISTERESIS), recolectar);
        fsm.add(poder,         new PeligroTransition(), supervivencia);
        fsm.add(poder,         new PorRevertirTransition(), supervivencia);
        fsm.add(poder,         new SinPoderTransition(), recolectar);
        fsm.ready(recolectar);

        if (DEBUG) {
            gRaiz.showInFrame(new Dimension(640, 360));
            gRecolectar.showInFrame(new Dimension(480, 320));
            gSupervivencia.showInFrame(new Dimension(320, 240));
            gPoder.showInFrame(new Dimension(320, 240));
        }
    }

    /** El observador hay que añadirlo antes que los estados, como en Ghosts. */
    private static GraphFSMObserver observar(FSM maquina, String nombre) {
        if (!DEBUG)
            return null;
        GraphFSMObserver grafo = new GraphFSMObserver(nombre);
        maquina.addObserver(grafo);
        return grafo;
    }

    /** NuevoNivel: todas las máquinas a su estado inicial (RECOLECTAR / IrAPildoraRentable). */
    private void reiniciar() {
        recolectar.reiniciar();
        supervivencia.reiniciar();
        poder.reiniciar();
        fsm.reset();
        memoria.reiniciar();
    }

    @Override
    public void preCompute(String opponent) {
        reiniciar();                            // cada partida empieza en el estado inicial
        nivelAnterior = -1;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        if (game.getCurrentLevel() != nivelAnterior || game.wasPacManEaten()) {
            reiniciar();
            nivelAnterior = game.getCurrentLevel();
            MapaNivel.de(game);                 // preproceso del laberinto nuevo
        }
        MsPacManInput in = new MsPacManInput(game);
        MOVE move = fsm.run(in);
        return (move == null) ? MOVE.NEUTRAL : move;
    }
}
