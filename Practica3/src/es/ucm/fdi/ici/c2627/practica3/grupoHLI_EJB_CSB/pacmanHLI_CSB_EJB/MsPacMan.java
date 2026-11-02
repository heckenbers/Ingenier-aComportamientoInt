package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB;

import java.util.HashMap;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MapaNivel;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MemoriaCebo;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.PacManAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.Turno;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.UltimaAccion;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.CazarFantasmaAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.EscapeDeEmergenciaAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.EvadirAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.IrAPildoraRentableAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.LimpiarZonaAisladaAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.PrepararCeboAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.pacmanHLI_CSB_EJB.mspacman.actions.RecolectarEnPoderAction;
import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RulesAction;
import pacman.controllers.PacmanController;
import pacman.game.Constants;
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
 *   mspacman-v1.clp  + el cebo (esperar junto a la píldora de poder)
 *   mspacman-v2.clp  + limpiar la zona aislada de píldoras mientras es barato
 *   mspacman-v3.clp  v1 + final de nivel (recolectar sin reservas con pocas píldoras)
 *   mspacman-v4.clp  v3 con UMBRAL_FINAL = 120 en vez de 70
 */
public class MsPacMan extends PacmanController {

    /**
     * Carpeta de los .clp DENTRO del classpath (Eclipse copia los .clp de src
     * a bin). Es texto: si cambia el paquete hay que cambiarla a mano.
     */
    public static final String RULES_PATH = "es/ucm/fdi/ici/c2627/practica3/grupoHLI_EJB_CSB/pacmanHLI_CSB_EJB/";
    /** Versión que se carga con el constructor sin argumentos (la del concurso). */
    public static final String VERSION_CONCURSO = "mspacman-v1.clp";

    private final RuleEngine motor;
    private final Turno turno = new Turno();
    private final UltimaAccion ultima = new UltimaAccion();
    private final MemoriaCebo cebo = new MemoriaCebo();
    private MapaNivel mapa;
    /**
     * Preproceso de los NUM_MAZES laberintos, hecho en el constructor. Medido:
     * un turno normal tarda ~0,2 ms, pero construir MapaNivel en el primer
     * turno de un laberinto nuevo llegaba a 39 ms (44 ms con dos partidas en
     * paralelo), al límite de los 40 ms. Así ningún turno lo construye.
     */
    private final MapaNivel[] mapas = new MapaNivel[Constants.NUM_MAZES];
    private int nivelAnterior = -1;

    public MsPacMan() {
        this(VERSION_CONCURSO);
    }

    /** Para Comparar: carga otra versión de las reglas. */
    public MsPacMan(String ficheroReglas) {
        setName("pacmanHLI_CSB_EJB");
        setTeam("HLI_EJB_CSB");

        HashMap<String, RulesAction> acciones = new HashMap<>();
        registrar(acciones, new EscapeDeEmergenciaAction(turno));
        registrar(acciones, new EvadirAction(turno));
        registrar(acciones, new CazarFantasmaAction(turno));
        registrar(acciones, new RecolectarEnPoderAction(turno));
        registrar(acciones, new IrAPildoraRentableAction(turno));
        registrar(acciones, new PrepararCeboAction(turno));     // v1; la v0 nunca la asierta
        registrar(acciones, new LimpiarZonaAisladaAction(turno)); // v2; v0 y v1 nunca la asiertan

        // El RuleEngine se crea SOLO aquí: Jess no carga dos .clp a la vez y
        // el concurso y Comparar juegan en paralelo.
        motor = new RuleEngine("MsPacMan", RULES_PATH + ficheroReglas, acciones);
        motor.addObserver(ultima);              // memoria entre turnos, no es una traza
        motor.addObserver(cebo);                // memoria del cebo (v1), tampoco es una traza

        for (int i = 0; i < mapas.length; i++)
            mapas[i] = MapaNivel.para(null, new Game(0, i));
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
        cebo.olvidar();
        nivelAnterior = -1;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        // Como en la Práctica 2: al cambiar de nivel o morir, se empieza de cero.
        if (game.getCurrentLevel() != nivelAnterior || game.wasPacManEaten()) {
            ultima.olvidar();
            cebo.olvidar();
            nivelAnterior = game.getCurrentLevel();
        }
        int laberinto = game.getMazeIndex();
        MapaNivel preparado = (laberinto >= 0 && laberinto < mapas.length) ? mapas[laberinto] : null;
        mapa = MapaNivel.para(preparado != null ? preparado : mapa, game);   // recalcula solo si no coincide

        MsPacManInput in = new MsPacManInput(game, mapa, ultima, cebo);
        turno.set(in);
        motor.reset();
        motor.assertFacts(in.getFacts());
        return motor.run(game);
    }
}
