package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos;

import java.util.HashMap;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.Turno;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.UltimaAccion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions.CazarFantasmaAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions.EscapeDeEmergenciaAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions.EvadirAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions.IrAPildoraRentableAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanCarlos.mspacman.actions.RecolectarEnPoderAction;
import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RulesAction;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Ms. Pac-Man por reglas (Práctica 3), traducida de la HFSM de la Práctica 2.
 *
 * Cada turno: el Input mide el juego, Jess decide con el .clp y la acción
 * Java del ACTION de mayor priority calcula el movimiento.
 *
 * Versiones (un .clp por versión, mismo Input y mismas acciones):
 *   mspacman-v0.clp  supervivencia + poder + recolección por defecto
 */
public class MsPacMan extends PacmanController {

    /**
     * Carpeta de los .clp DENTRO del classpath (Eclipse copia los .clp de src
     * a bin). Es texto: si cambia el paquete hay que cambiarla a mano.
     */
    public static final String RULES_PATH = "es/ucm/fdi/ici/c2627/practica3/grupoHLI_EJB_CSB/pacmanCarlos/";
    /** Versión que se carga con el constructor sin argumentos (la del concurso). */
    public static final String VERSION_CONCURSO = "mspacman-v0.clp";

    private final RuleEngine motor;
    private final Turno turno = new Turno();
    private final UltimaAccion ultima = new UltimaAccion();
    private MapaNivel mapa;
    private int nivelAnterior = -1;

    public MsPacMan() {
        this(VERSION_CONCURSO);
    }

    /** Para Comparar: carga otra versión de las reglas. */
    public MsPacMan(String ficheroReglas) {
        setName("MsPacMan HLI_EJB_CSB");
        setTeam("HLI_EJB_CSB");

        HashMap<String, RulesAction> acciones = new HashMap<>();
        registrar(acciones, new EscapeDeEmergenciaAction(turno));
        registrar(acciones, new EvadirAction(turno));
        registrar(acciones, new CazarFantasmaAction(turno));
        registrar(acciones, new RecolectarEnPoderAction(turno));
        registrar(acciones, new IrAPildoraRentableAction(turno));

        // El RuleEngine se crea SOLO aquí: Jess no carga dos .clp a la vez y
        // el concurso y Comparar juegan en paralelo.
        motor = new RuleEngine("MsPacMan", RULES_PATH + ficheroReglas, acciones);
        motor.addObserver(ultima);              // memoria entre turnos, no es una traza
    }

    private static void registrar(HashMap<String, RulesAction> mapa, PacManAction a) {
        mapa.put(a.getActionId(), a);
    }

    /** Para TrazaReglas y las pruebas. */
    public RuleEngine getMotor() {
        return motor;
    }

    @Override
    public void preCompute(String opponent) {
        ultima.olvidar();
        nivelAnterior = -1;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        // Como en la Práctica 2: al cambiar de nivel o morir, se empieza de cero.
        if (game.getCurrentLevel() != nivelAnterior || game.wasPacManEaten()) {
            ultima.olvidar();
            nivelAnterior = game.getCurrentLevel();
        }
        mapa = MapaNivel.para(mapa, game);

        MsPacManInput in = new MsPacManInput(game, mapa, ultima.get(), ultima.getTicksSinPeligro());
        turno.set(in);
        motor.reset();
        motor.assertFacts(in.getFacts());
        return motor.run(game);
    }
}
