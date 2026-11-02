package es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;

import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.GhostsInput;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.BloquearPPAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.CazarAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.CerrarPasoAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.DispersarseAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.HuidaEficienteAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.HuirAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.HuirDePPAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.InterceptarAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.PerseguirAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.RefugiarseAction;
import es.ucm.fdi.ici.c2627.practica3.grupoHLI_EJB_CSB.ghostsHLI_CSB_EJB.ghosts.actions.VigilarPPAction;
import es.ucm.fdi.ici.rules.RuleEngine;
import es.ucm.fdi.ici.rules.RulesAction;
import pacman.controllers.GhostController;
import pacman.game.Constants.GHOST;
import pacman.game.Constants.MOVE;
import pacman.game.Game;

/**
 * Fantasmas con reglas: un motor por fantasma, todos con la misma base de reglas (.clp).
 * Cada motor recibe los hechos comunes (GhostsInput) más dos propios:
 *   (YO (nombre X))       quién soy, para que las reglas se refieran a "mi" FANTASMA
 *   (ULTIMA (accion Y))   la acción que ganó en mi turno anterior: memoria para la histéresis
 */
public class Ghosts extends GhostController {

    private static final String RULES_PATH = "es/ucm/fdi/ici/c2627/practica3/grupoHLI_EJB_CSB/ghostsHLI_CSB_EJB/";

    private final EnumMap<GHOST, RuleEngine> motores = new EnumMap<>(GHOST.class);

    /** La última acción de cada fantasma. La rellena el propio motor (es un observador suyo). */
    private final EnumMap<GHOST, UltimaAccion> ultimas = new EnumMap<>(GHOST.class);

    public Ghosts() {
        this("ghosts-v1.clp");                    // la versión que va al concurso
    }

    public Ghosts(String ficheroReglas) {
        setName("ghostsHLI_CSB_EJB");
        setTeam("HLI_EJB_CSB");
        for (GHOST g : GHOST.values()) {
            HashMap<String, RulesAction> acciones = new HashMap<>();   // las de ESTE fantasma
            for (RulesAction a : accionesDe(g))
                acciones.put(a.getActionId(), a);                       // cada id se escribe una sola vez

            RuleEngine motor = new RuleEngine(g.name(), RULES_PATH + ficheroReglas, acciones);
            UltimaAccion ultima = new UltimaAccion();
            motor.addObserver(ultima);
            motores.put(g, motor);
            ultimas.put(g, ultima);
        }
    }

    /**
     * Todas las acciones que puede elegir un fantasma. Las reglas las nombran por su id en
     * (ACTION (id ...)). PerseguirAction y HuirAction son las del ejemplo (las usan v0 y v1).
     */
    private static RulesAction[] accionesDe(GHOST g) {
        return new RulesAction[] {
                // del ejemplo
                new PerseguirAction(g), new HuirAction(g),
                // NORMAL: cazar a Ms. Pac-Man
                new InterceptarAction(g), new CazarAction(g), new CerrarPasoAction(g), new VigilarPPAction(g),
                // PELIGRO POR PÍLDORA
                new BloquearPPAction(g), new HuirDePPAction(g),
                // COMESTIBLE
                new HuidaEficienteAction(g), new RefugiarseAction(g), new DispersarseAction(g) };
    }

    public RuleEngine getMotor(GHOST g) { return motores.get(g); }

    /** Antes de cada partida: nadie recuerda la partida anterior. */
    @Override
    public void preCompute(String opponent) {
        for (UltimaAccion u : ultimas.values()) u.olvidar();
    }

    @Override
    public EnumMap<GHOST, MOVE> getMove(Game game, long timeDue) {
        GhostsInput input = new GhostsInput(game);               // una vez para los cuatro
        EnumMap<GHOST, MOVE> movimientos = new EnumMap<>(GHOST.class);
        for (GHOST g : GHOST.values()) {
            if (!game.doesGhostRequireAction(g)) continue;       // fuera de los cruces no hay nada que decidir
            RuleEngine motor = motores.get(g);
            motor.reset();
            motor.assertFacts(input.getFacts());
            motor.assertFacts(List.of(
                    "(YO (nombre " + g + "))",
                    "(ULTIMA (accion " + ultimaDe(g) + "))"));
            movimientos.put(g, motor.run(game));
        }
        return movimientos;
    }

    /** La última acción de g, o 'ninguna' al empezar la partida. */
    private String ultimaDe(GHOST g) {
        String u = ultimas.get(g).get();
        return (u == null || u.isEmpty()) ? "ninguna" : u;
    }
}