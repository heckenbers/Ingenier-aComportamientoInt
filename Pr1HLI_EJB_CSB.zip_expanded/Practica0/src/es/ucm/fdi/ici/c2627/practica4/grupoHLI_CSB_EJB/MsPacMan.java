package es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB;

import es.ucm.fdi.ici.Action;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Contexto;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.MsPacManInput;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.Percepcion;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.CazarAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.ComerCeboAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.EscaparAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.EsperarCeboAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.EvadirAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.IrAPildoraAction;
import es.ucm.fdi.ici.c2627.practica4.grupoHLI_CSB_EJB.mspacman.actions.RecolectarAction;
import es.ucm.fdi.ici.fuzzy.FuzzyEngine;
import pacman.controllers.PacmanController;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Ms. Pac-Man borrosa. En cada turno, Java calcula la percepción (amenaza, margen de huida,
 * plan de caza, cebo...), MsPacManInput la convierte en siete números, el FCL da un valor a
 * cada acción y MaxActionSelector elige la más alta. Las acciones son los estados hoja de la
 * V3 de la Práctica 2; lo que antes decidían las transiciones de la HFSM, ahora lo decide el FCL.
 */
public class MsPacMan extends PacmanController {

    // Una ruta del disco, no del classpath: Eclipse copia el .fcl a bin/ y lanza desde la carpeta del proyecto
    static final String FCL_PATH = "bin/es/ucm/fdi/ici/c2627/practica4/grupoHLI_CSB_EJB/";

    /** La versión que va al concurso. */
    public static final String FCL_CONCURSO = "mspacman-v5.fcl";

    private final FuzzyEngine motor;
    private final Contexto contexto = new Contexto();
    private int nivelAnterior = -1;

    public MsPacMan() {
        this(FCL_CONCURSO);
    }

    /** Cada versión, un .fcl distinto, con el mismo Input y las mismas acciones. */
    public MsPacMan(String ficheroFcl) {
        setName("MsPacMan grupoHLI_CSB_EJB");
        setTeam("grupoHLI_CSB_EJB");
        // el orden decide los empates: lo más prudente primero
        Action[] acciones = {
                new EscaparAction(contexto), new EvadirAction(contexto), new CazarAction(contexto),
                new ComerCeboAction(contexto), new EsperarCeboAction(contexto), new IrAPildoraAction(contexto),
                new RecolectarAction(contexto) };
        motor = new FuzzyEngine("MSPACMAN", FCL_PATH + ficheroFcl, "MsPacMan", new MaxActionSelector(acciones));
    }

    /** Para engancharle un observador desde fuera. */
    public FuzzyEngine getMotor() { return motor; }

    @Override
    public void preCompute(String opponent) {
        contexto.reiniciar();
        nivelAnterior = -1;
    }

    @Override
    public MOVE getMove(Game game, long timeDue) {
        if (game.getCurrentLevel() != nivelAnterior || game.wasPacManEaten()) {
            contexto.reiniciar();
            nivelAnterior = game.getCurrentLevel();
        }
        Percepcion p = contexto.actualizar(game);
        return motor.run(new MsPacManInput(game, p, contexto.getMemoria()).getFuzzyValues(), game);
    }
}
