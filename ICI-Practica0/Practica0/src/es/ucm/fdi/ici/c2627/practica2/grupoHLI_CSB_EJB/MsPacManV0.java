package es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.ComerFantasmaAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.ComerPildoraAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.EsperarJuntoPildoraPoderAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.EsquivarPildoraPoderAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.EvitarCaminoAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.HuirACruceAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.HuirHaciaPildoraPoderAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.IrPildoraPoderAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.LimpiarUltimasAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.MsPacManAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.actions.PatrullarAction;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.AndTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.ComestibleCercaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.EsperaAgotadaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.EtiquetadaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.FantasmaCercaDePildoraPoderTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.FantasmaPeligrosoCercaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.JuntoAPildoraPoderTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.NotTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.OrTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.PildoraPoderAlLadoTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.PildoraPoderCercaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.PildoraVigiladaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.PocasPildorasTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.PocosFantasmasCercaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.RutaPoderInterceptadaTransition;
import es.ucm.fdi.ici.c2627.practica2.grupoHLI_CSB_EJB.mspacman.transitions.VentajaEnPildoraPoderTransition;
import es.ucm.fdi.ici.fsm.CompoundState;
import es.ucm.fdi.ici.fsm.FSM;
import es.ucm.fdi.ici.fsm.SimpleState;
import es.ucm.fdi.ici.fsm.Transition;
import es.ucm.fdi.ici.fsm.observers.ConsoleFSMObserver;
import es.ucm.fdi.ici.fsm.observers.GraphFSMObserver;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * v0 · El diseño tal cual, sin mejoras. Máquina jerárquica de Ms. Pac-Man con 10 estados simples.
 * <p>
 * Primer nivel, de mayor a menor prioridad: HUIR {HuirACruce, HuirHaciaPildoraPoder}, ComerFantasma,
 * PODER {IrPildoraPoder, EvitarCamino, EsperarJuntoPildoraPoder} y COMER {ComerPildora (inicial),
 * Patrullar, EsquivarPildoraPoder, LimpiarUltimas}. Las transiciones salen de cada estado en orden de
 * prioridad. La histéresis se escribe sin flechas de un estado a sí mismo: una flecha hacia un estado
 * de menor prioridad solo se mira cuando ya no se cumple la condición de permanencia del actual.
 * <p>
 * Con Constantes.DEBUG a true se abre una ventana con el grafo de cada máquina y se escribe en consola
 * cada transición. Medidas: pendiente (ver diario.md).
 */
public class MsPacManV0 extends PacmanController {

    private final FSM fsm;
    private final FSM huirFsm;
    private final FSM poderFsm;
    private final FSM comerFsm;

    /** Grafos de las máquinas, solo se rellena con DEBUG. Atributo de la instancia, no estático. */
    private final List<GraphFSMObserver> grafos = new ArrayList<>();

    /** Las acciones de los 10 estados simples, para que una versión posterior (V1) pueda configurarlas. */
    private final List<MsPacManAction> acciones = new ArrayList<>();

    public MsPacManV0() {
        // ---- Estados simples (una acción cada uno)
        SimpleState comerPildora = estado(new ComerPildoraAction());
        SimpleState patrullar = estado(new PatrullarAction());
        SimpleState esquivar = estado(new EsquivarPildoraPoderAction());
        SimpleState limpiar = estado(new LimpiarUltimasAction());
        SimpleState irPoder = estado(new IrPildoraPoderAction());
        SimpleState evitar = estado(new EvitarCaminoAction());
        SimpleState esperar = estado(new EsperarJuntoPildoraPoderAction());
        SimpleState huirCruce = estado(new HuirACruceAction());
        SimpleState huirPoder = estado(new HuirHaciaPildoraPoderAction());
        SimpleState cazar = estado(new ComerFantasmaAction());

        // ---- COMER: el estado inicial es ComerPildora (Ms. Pac-Man aparece en un nodo cualquiera)
        comerFsm = crearFsm("COMER");
        Transition vigilada = new PildoraVigiladaTransition(Constantes.PATRULLA_ENTRADA);
        Transition alLado = new PildoraPoderAlLadoTransition(Constantes.ESQUIVAR_ENTRADA);
        Transition pocas = new PocasPildorasTransition(Constantes.LIMPIAR_ENTRADA);
        comerFsm.add(comerPildora, vigilada, patrullar);
        comerFsm.add(comerPildora, alLado, esquivar);
        comerFsm.add(comerPildora, pocas, limpiar);
        comerFsm.add(patrullar, new NotTransition(new PildoraVigiladaTransition(Constantes.PATRULLA_SALIDA)), comerPildora);
        comerFsm.add(esquivar, new NotTransition(new PildoraPoderAlLadoTransition(Constantes.ESQUIVAR_SALIDA)), comerPildora);
        comerFsm.add(limpiar, new NotTransition(new PocasPildorasTransition(Constantes.LIMPIAR_SALIDA)), comerPildora);
        comerFsm.ready(comerPildora);

        // ---- PODER: ir a la píldora, rodear un fantasma que corta el camino o esperar junto a ella
        poderFsm = crearFsm("PODER");
        Transition pocosFantasmas = new PocosFantasmasCercaTransition(Constantes.ESPERA_FANTASMAS, Constantes.ESPERA_DISTANCIA);
        poderFsm.add(irPoder, new RutaPoderInterceptadaTransition(Constantes.EVITAR_ENTRADA), evitar);
        poderFsm.add(irPoder, new AndTransition(new JuntoAPildoraPoderTransition(Constantes.ESPERA_JUNTO, true), pocosFantasmas), esperar);
        poderFsm.add(evitar, new NotTransition(new FantasmaCercaDePildoraPoderTransition(Constantes.EVITAR_SALIDA)), irPoder);
        poderFsm.add(esperar, new OrTransition(new NotTransition(pocosFantasmas),
                new EsperaAgotadaTransition(Constantes.ESPERA_MAX_TURNOS)), irPoder);
        poderFsm.ready(irPoder);

        // ---- HUIR: a un cruce lejano, o hacia la píldora de poder si se llega antes que los fantasmas
        huirFsm = crearFsm("HUIR");
        huirFsm.add(huirCruce, new VentajaEnPildoraPoderTransition(Constantes.HUIR_PODER_ENTRADA), huirPoder);
        huirFsm.add(huirPoder, new NotTransition(new VentajaEnPildoraPoderTransition(Constantes.HUIR_PODER_SALIDA)), huirCruce);
        huirFsm.ready(huirCruce);

        // ---- Primer nivel
        CompoundState comer = new CompoundState("COMER", comerFsm);
        CompoundState poder = new CompoundState("PODER", poderFsm);
        CompoundState huir = new CompoundState("HUIR", huirFsm);

        Transition entrarHuir = new FantasmaPeligrosoCercaTransition(Constantes.HUIR_ENTRADA);
        Transition entrarCazar = new ComestibleCercaTransition(Constantes.CAZAR_ENTRADA, false);
        Transition entrarPoder = new PildoraPoderCercaTransition(Constantes.PODER_LIMITE, true);
        Transition seguirHuyendo = new FantasmaPeligrosoCercaTransition(Constantes.HUIR_SALIDA);
        Transition seguirCazando = new ComestibleCercaTransition(Constantes.CAZAR_SALIDA, true);
        Transition seguirPoder = new PildoraPoderCercaTransition(Constantes.PODER_SALIDA, true);

        fsm = crearFsm("MsPacMan");
        // Desde COMER: se entra al estado cuya condición de entrada se cumpla, por prioridad
        fsm.add(comer, desde("COMER", entrarHuir), huir);
        fsm.add(comer, desde("COMER", entrarCazar), cazar);
        fsm.add(comer, desde("COMER", entrarPoder), poder);
        // Desde PODER: lo más prioritario manda; si ya no se cumple la permanencia, vuelve a comer
        fsm.add(poder, desde("PODER", entrarHuir), huir);
        fsm.add(poder, desde("PODER", entrarCazar), cazar);
        fsm.add(poder, desde("PODER", new NotTransition(seguirPoder)), comer);
        // Desde ComerFantasma
        fsm.add(cazar, desde("CAZAR", entrarHuir), huir);
        fsm.add(cazar, desde("CAZAR", new AndTransition(new NotTransition(seguirCazando), entrarPoder)), poder);
        fsm.add(cazar, desde("CAZAR", new NotTransition(seguirCazando)), comer);
        // Desde HUIR
        fsm.add(huir, desde("HUIR", new AndTransition(new NotTransition(seguirHuyendo), entrarCazar)), cazar);
        fsm.add(huir, desde("HUIR", new AndTransition(new NotTransition(seguirHuyendo), entrarPoder)), poder);
        fsm.add(huir, desde("HUIR", new NotTransition(seguirHuyendo)), comer);
        fsm.ready(comer);

        if (Constantes.DEBUG) {
            mostrarGrafos();
        }
    }

    /** Crea el estado simple de una acción y la guarda en la lista de acciones. */
    private SimpleState estado(MsPacManAction accion) {
        acciones.add(accion);
        return new SimpleState(accion);
    }

    /** @return las acciones de los 10 estados simples (las mismas instancias que usa la máquina) */
    protected List<MsPacManAction> getAcciones() {
        return acciones;
    }

    /** Distingue por el estado de origen las flechas con la misma condición, para que el grafo no las confunda. */
    private static Transition desde(String origen, Transition t) {
        return new EtiquetadaTransition(t, origen);
    }

    /**
     * Crea una máquina y, con DEBUG, le añade los observadores de inmediato: tiene que ser antes del primer
     * add(), porque el grafo se construye con los avisos de add() y, si llegase tarde, saldría vacío y
     * fallaría con una NullPointerException en el primer cambio de estado.
     */
    private FSM crearFsm(String nombre) {
        FSM maquina = new FSM(nombre);
        if (Constantes.DEBUG) {
            GraphFSMObserver grafo = new GraphFSMObserver(nombre);
            maquina.addObserver(grafo);
            maquina.addObserver(new ConsoleFSMObserver(nombre));
            grafos.add(grafo);
        }
        return maquina;
    }

    /** Una sola ventana con el grafo de cada máquina (COMER, PODER, HUIR y la de fuera). */
    private void mostrarGrafos() {
        JFrame ventana = new JFrame(getName());
        ventana.setLayout(new GridLayout(2, 2));
        for (GraphFSMObserver grafo : grafos) {
            ventana.add(grafo.getAsPanel(true, new Dimension(420, 320)));
        }
        ventana.pack();
        ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ventana.setVisible(true);
    }

    /** Cada partida empieza con todas las máquinas, también las de dentro de los compuestos, en su estado inicial. */
    @Override
    public void preCompute(String opponent) {
        reiniciar();
    }

    private void reiniciar() {
        comerFsm.reset();
        poderFsm.reset();
        huirFsm.reset();
        fsm.reset();
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        if (game.getTotalTime() == 0) {
            reiniciar();                                   // por si el modo de ejecución no llama a preCompute
        }
        MOVE mov = fsm.run(new MsPacManInput(game));
        return mov == null ? MOVE.NEUTRAL : mov;
    }

    @Override
    public String getName() {
        return "MsPacMan FSM grupoHLI_CSB_EJB V0";
    }
}